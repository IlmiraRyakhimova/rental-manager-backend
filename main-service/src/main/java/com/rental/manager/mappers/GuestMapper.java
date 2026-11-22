package com.rental.manager.mappers;

import com.rental.manager.dto.requestdto.GuestPatchRequestDto;
import com.rental.manager.dto.requestdto.GuestRequestDto;
import com.rental.manager.dto.responsedto.GuestResponseDto;
import com.rental.manager.entities.Guest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GuestMapper {

    public GuestResponseDto toDto(Guest entity) {
        return GuestResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .dateOfBirth(entity.getDateOfBirth())
                .nationality(entity.getNationality())
                .passportNumber(entity.getPassportNumber())
                .passportIssueDate(entity.getPassportIssueDate())
                .notes(entity.getNotes())
                .build();

    }


    public Guest toEntity(GuestRequestDto dto) {
        return Guest.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .nationality(dto.getNationality())
                .passportNumber(dto.getPassportNumber())
                .passportIssueDate(dto.getPassportIssueDate())
                .notes(dto.getNotes())
                .build();
    }

    public void updateEntity(Guest entity, GuestPatchRequestDto dto) {
        if(dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if(dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if(dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfBirth() != null) {
            entity.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getNationality() != null) {
            entity.setNationality(dto.getNationality());
        }
        if (dto.getPassportNumber() != null) {
            entity.setPassportNumber(dto.getPassportNumber());
        }
        if (dto.getPassportIssueDate() != null) {
            entity.setPassportIssueDate(dto.getPassportIssueDate());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }

    public void updateEntity(Guest entity, GuestRequestDto dto) {
        entity.setName(dto.getName());
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfBirth() != null) {
            entity.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getNationality() != null) {
            entity.setNationality(dto.getNationality());
        }
        if (dto.getPassportNumber() != null) {
            entity.setPassportNumber(dto.getPassportNumber());
        }
        if (dto.getPassportIssueDate() != null) {
            entity.setPassportIssueDate(dto.getPassportIssueDate());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}
