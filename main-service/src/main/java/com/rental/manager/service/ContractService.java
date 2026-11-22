package com.rental.manager.service;

import java.util.UUID;

public interface ContractService {

    enum ContractFormat { DOCX, PDF }

    /**
     * Генерирует договор аренды по данным бронирования (гость, апартаменты, условия)
     * на основе шаблона "Lease Agreement".
     */
    byte[] generateContract(UUID bookingId, ContractFormat format);
}
