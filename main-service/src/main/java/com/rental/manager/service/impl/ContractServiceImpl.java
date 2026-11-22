package com.rental.manager.service.impl;

import com.rental.manager.entities.Address;
import com.rental.manager.entities.Apartment;
import com.rental.manager.entities.Booking;
import com.rental.manager.entities.Guest;
import com.rental.manager.repository.BookingRepository;
import com.rental.manager.service.ContractService;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String UNDERSCORES = "________";

    private final BookingRepository bookingRepository;

    @Value("${app.contract.lessor-company}")
    private String lessorCompany;

    @Value("${app.contract.lessor-registration-number}")
    private String lessorRegistrationNumber;

    @Value("${app.contract.lessor-address}")
    private String lessorAddress;

    @Value("${app.contract.lessor-director}")
    private String lessorDirector;

    @Value("${app.contract.currency}")
    private String currency;

    @Value("${app.contract.check-in-time}")
    private String checkInTime;

    @Value("${app.contract.check-out-time}")
    private String checkOutTime;

    @Value("${app.contract.smoking-fine}")
    private String smokingFine;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateContract(UUID bookingId, ContractFormat format) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено с id: " + bookingId));

        try (XWPFDocument document = buildDocument(booking);
             ByteArrayOutputStream docxOut = new ByteArrayOutputStream()) {

            document.write(docxOut);
            byte[] docxBytes = docxOut.toByteArray();

            byte[] result = format == ContractFormat.PDF ? convertToPdf(docxBytes) : docxBytes;
            log.info("Договор аренды ({}) сгенерирован для бронирования {}", format, booking.getBookingCode());
            return result;
        } catch (IOException e) {
            log.error("Ошибка генерации договора для бронирования {}: {}", bookingId, e.getMessage(), e);
            throw new IllegalStateException("Не удалось сгенерировать договор: " + e.getMessage(), e);
        }
    }

    /**
     * Конвертация выполняется по сохранённому DOCX: конвертер читает части пакета
     * (styles.xml и др.), которые записываются только при сериализации документа.
     */
    private byte[] convertToPdf(byte[] docxBytes) throws IOException {
        try (XWPFDocument saved = new XWPFDocument(new ByteArrayInputStream(docxBytes));
             ByteArrayOutputStream pdfOut = new ByteArrayOutputStream()) {
            PdfConverter.getInstance().convert(saved, pdfOut, PdfOptions.create());
            return pdfOut.toByteArray();
        }
    }

    private XWPFDocument buildDocument(Booking booking) {
        Guest guest = booking.getMainGuest();
        Apartment apartment = booking.getApartment();

        XWPFDocument doc = new XWPFDocument();
        // Заполненная часть styles.xml и параметры секции (A4) обязательны для конвертации в PDF
        CTStyles ctStyles = CTStyles.Factory.newInstance();
        ctStyles.addNewDocDefaults();
        doc.createStyles().setStyles(ctStyles);

        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));
        pageSize.setH(BigInteger.valueOf(16838));

        title(doc, "Lease Agreement");

        // Преамбула: стороны договора
        XWPFParagraph intro = paragraph(doc);
        text(intro, "Residential Lease Agreement was signed on ");
        bold(intro, formatDate(booking.getCreatedAt() != null ? booking.getCreatedAt().toLocalDate() : LocalDate.now()));
        text(intro, " between:");

        XWPFParagraph lessor = paragraph(doc);
        boldUnderlined(lessor, "%s, cooperation number %s, address: %s, Presented by director %s"
                .formatted(lessorCompany, lessorRegistrationNumber, lessorAddress, lessorDirector));
        text(lessor, " hereinafter in this contract a party referred to as the \"");
        bold(lessor, "Lessor");
        text(lessor, "\"");

        XWPFParagraph tenant = paragraph(doc);
        text(tenant, "and ");
        bold(tenant, guestName(guest));
        text(tenant, ", date of birthday " + formatDate(guest != null ? guest.getDateOfBirth() : null)
                + ", country " + orBlank(guest != null ? guest.getNationality() : null)
                + ", Passport no. " + orBlank(guest != null ? guest.getPassportNumber() : null)
                + ", issued " + formatDate(guest != null ? guest.getPassportIssueDate() : null) + ",");

        XWPFParagraph term = paragraph(doc);
        text(term, "further referred to as the \"");
        bold(term, "Tenant");
        text(term, "\". Both parties agree for the rental term from ");
        boldUnderlined(term, formatDate(booking.getCheckInDate()) + " till " + formatDate(booking.getCheckOutDate()));
        text(term, " with the following statements:");

        // Объект договора
        heading(doc, "Object of the Agreement");
        XWPFParagraph object1 = paragraph(doc);
        text(object1, "The ");
        bold(object1, "Lessor");
        text(object1, " agrees to rent to the ");
        bold(object1, "Tenant");
        text(object1, ", and the ");
        bold(object1, "Tenant");
        text(object1, " agrees to rent from the ");
        bold(object1, "Lessor");
        text(object1, ":");

        XWPFParagraph object2 = paragraph(doc);
        boldUnderlined(object2, propertyDescription(apartment));
        text(object2, " referred to as the rental place for the purpose of residence. ");
        bold(object2, "Lessor");
        text(object2, " guarantee that above-mentioned property is under his management and is carrying all the rights and responsibility hereof.");

        // Стоимость аренды
        heading(doc, "Rental Cost");
        XWPFParagraph cost = paragraph(doc);
        text(cost, "The rental price of a fore mentioned apartment is: ");
        boldUnderlined(cost, formatMoney(booking.getTotalPrice()));
        text(cost, " " + currency + " for the period ");
        boldUnderlined(cost, "from " + formatDate(booking.getCheckInDate()) + " till " + formatDate(booking.getCheckOutDate()) + ".");

        XWPFParagraph deposit = paragraph(doc);
        text(deposit, "The Rental price doesn't include cleaning service, electric payments and the property functionality expenses. The security deposit for the apartment is ");
        bold(deposit, formatMoney(booking.getSecurityDeposit()) + " " + depositCurrency(booking) + ".");

        // Общие условия и политика отмены
        heading(doc, "General Terms and Cancellation Policy");
        XWPFParagraph terms = paragraph(doc);
        text(terms, "Check in " + checkInTime + " or set by personal agreement and check out time " + checkOutTime
                + ". The security deposit should be paid on the check in date and will be returned by the ");
        bold(terms, "Lessor");
        text(terms, " on the day of the check out, if there will be no damages cases. The ");
        bold(terms, "Tenant");
        text(terms, " is to pay the ");
        boldUnderlined(terms, "check-out cleaning in amount " + formatMoney(booking.getCleaningFee()) + " " + currency);
        text(terms, ". All advance payments are non-refundable under any circumstances. In the event of a no-show, "
                + "cancellation charges will be equivalent to 100% of the total cost. Booking modifications are permitted once, "
                + "subject to room availability, provided they are made at least 30 days before the scheduled arrival. "
                + "Blackout dates are exempted from this policy. Early termination of the contract will result in forfeiture "
                + "of all advance payments. Failure to pay the full rent will result in the security deposit not being refunded. "
                + "In case of cancellation of the further agreement by the ");
        bold(terms, "Tenant");
        text(terms, " before expiration date, the ");
        bold(terms, "Lessor");
        text(terms, " has the right to do not recalculate the cost for unused days. "
                + "Smoking inside the property is highly prohibited fine " + smokingFine + ".");

        // Коммунальные платежи
        heading(doc, "Unit charge");
        XWPFParagraph unitIntro = paragraph(doc);
        text(unitIntro, "The ");
        bold(unitIntro, "Tenant");
        text(unitIntro, " is responsible for paying the utility in further conditions:");

        XWPFParagraph electric = paragraph(doc);
        text(electric, "Electric price is ");
        boldUnderlined(electric, formatMoney(booking.getElectricityRate()));
        text(electric, " " + currency + " per unit. Start from ");
        boldUnderlined(electric, UNDERSCORES);
        text(electric, " counter indicator.");

        XWPFParagraph water = paragraph(doc);
        text(water, "Water price is ");
        boldUnderlined(water, formatMoney(booking.getWaterRate()));
        text(water, " " + currency + " per unit. Start from ");
        boldUnderlined(water, UNDERSCORES);
        text(water, " counter indicator.");

        // Ответственность
        heading(doc, "Liability");
        XWPFParagraph liability = paragraph(doc);
        text(liability, "The property is controlled and checked by the ");
        bold(liability, "Lessor");
        text(liability, ". For any complaint, the ");
        bold(liability, "Tenant");
        text(liability, " commits to inform the Lessor within 48hr. The Tenant also commits to collaborate with the ");
        bold(liability, "Lessor");
        text(liability, " to try to solve the issue. Tenant is responsible for any technical aspects as well as necessary repairs and related expenses caused on his accommodation.");

        heading(doc, "Legal liability");
        XWPFParagraph legal = paragraph(doc);
        text(legal, "The ");
        bold(legal, "Tenant");
        text(legal, " and the ");
        bold(legal, "Lessor");
        text(legal, " both follow the local laws of Thailand, regulations of this Agreement and accept international "
                + "social norms and ethics. All the disputes, arising out of this Agreement, shall be settled by negotiation. "
                + "If the Parties are unable to resolve the dispute by negotiation it shall be submitted to the Court of Thailand "
                + "at the rental property registration and location in accordance with the local court settlement procedure.");

        XWPFParagraph copies = paragraph(doc);
        text(copies, "This ");
        bold(copies, "Agreement");
        text(copies, " is made in two identical copies and each party will retain one copy. Whereas both parties having read "
                + "and fully understood all the terms and provisions herein contained agree they are correct and in accordance "
                + "with their intentions they have hereunder affixed their signatures in the presence of witnesses bearing "
                + "important testimony hereof.");

        signatureBlock(doc);

        // Бронировочный депозит
        XWPFParagraph bookingDeposit = paragraph(doc);
        bold(bookingDeposit, "Booking deposit is " + formatMoney(booking.getSecurityDeposit()) + " " + depositCurrency(booking)
                + " should be paid before " + formatDate(booking.getDepositDueDate()) + " ___________________________");

        return doc;
    }

    private void signatureBlock(XWPFDocument doc) {
        XWPFTable table = doc.createTable(1, 2);
        table.setWidth("100%");
        table.removeBorders();

        // Явная сетка колонок — обязательна для конвертации таблицы в PDF
        CTTblGrid grid = table.getCTTbl().addNewTblGrid();
        grid.addNewGridCol().setW(BigInteger.valueOf(4513));
        grid.addNewGridCol().setW(BigInteger.valueOf(4513));

        XWPFTableCell lessorCell = table.getRow(0).getCell(0);
        lessorCell.removeParagraph(0);
        XWPFParagraph lp1 = lessorCell.addParagraph();
        text(lp1, "________________________");
        XWPFParagraph lp2 = lessorCell.addParagraph();
        bold(lp2, "Lessor / Agent");
        XWPFParagraph lp3 = lessorCell.addParagraph();
        boldUnderlined(lp3, "( " + lessorDirector + " )");

        XWPFTableCell tenantCell = table.getRow(0).getCell(1);
        tenantCell.removeParagraph(0);
        XWPFParagraph tp1 = tenantCell.addParagraph();
        tp1.setAlignment(ParagraphAlignment.RIGHT);
        text(tp1, "______________________ ");
        bold(tp1, "Tenant");
        XWPFParagraph tp2 = tenantCell.addParagraph();
        tp2.setAlignment(ParagraphAlignment.RIGHT);
        text(tp2, "_______________________________");
    }

    // === Помощники форматирования документа ===

    private void title(XWPFDocument doc, String value) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingAfter(300);
        XWPFRun run = p.createRun();
        run.setText(value);
        run.setBold(true);
        run.setFontSize(18);
    }

    private void heading(XWPFDocument doc, String value) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        p.setSpacingAfter(120);
        XWPFRun run = p.createRun();
        run.setText(value);
        run.setBold(true);
        run.setFontSize(13);
    }

    private XWPFParagraph paragraph(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(120);
        p.setAlignment(ParagraphAlignment.BOTH);
        return p;
    }

    private void text(XWPFParagraph p, String value) {
        XWPFRun run = p.createRun();
        run.setText(value);
    }

    private void bold(XWPFParagraph p, String value) {
        XWPFRun run = p.createRun();
        run.setText(value);
        run.setBold(true);
    }

    private void boldUnderlined(XWPFParagraph p, String value) {
        XWPFRun run = p.createRun();
        run.setText(value);
        run.setBold(true);
        run.setUnderline(UnderlinePatterns.SINGLE);
    }

    // === Помощники форматирования данных ===

    private String guestName(Guest guest) {
        return guest != null && guest.getName() != null ? guest.getName().toUpperCase() : UNDERSCORES;
    }

    private String orBlank(String value) {
        return value != null && !value.isBlank() ? value : UNDERSCORES;
    }

    private String propertyDescription(Apartment apartment) {
        if (apartment == null) {
            return UNDERSCORES;
        }
        StringBuilder sb = new StringBuilder();
        if (apartment.getTitle() != null && !apartment.getTitle().isBlank()) {
            sb.append(apartment.getTitle());
        }
        Address address = apartment.getAddress();
        if (address != null) {
            String addr = address.getAddress();
            if (addr != null && !addr.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(addr);
            }
        }
        if (!sb.isEmpty()) {
            sb.append(",");
        }
        return sb.isEmpty() ? UNDERSCORES : sb.toString();
    }

    private String depositCurrency(Booking booking) {
        return booking.getSecurityDepositCurrency() != null && !booking.getSecurityDepositCurrency().isBlank()
                ? booking.getSecurityDepositCurrency()
                : "THB";
    }

    private String formatDate(LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : UNDERSCORES;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return UNDERSCORES;
        }
        boolean wholeNumber = value.stripTrailingZeros().scale() <= 0;
        return wholeNumber
                ? String.format("%,d", value.longValue())
                : String.format("%,.2f", value);
    }
}
