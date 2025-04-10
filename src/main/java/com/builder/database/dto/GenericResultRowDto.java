package com.builder.database.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericResultRowDto {

    @Builder.Default
    private Map<String, Object> fields = new HashMap<>();

//    public static GenericResultRowDto fromMap(Map<String, Object> map) {
//        return GenericResultRowDto.builder()
//                .fields(map)
//                .build();
//    }

    @JsonAnyGetter
    public Map<String, Object> getFields() {
        return fields;
    }

}
