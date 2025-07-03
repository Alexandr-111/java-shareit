package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ItemDtoShort {
        private String name;
        private String description;

        @Builder
        public ItemDtoShort(String name, String description) {
            this.name = name;
            this.description = description;
        }
}