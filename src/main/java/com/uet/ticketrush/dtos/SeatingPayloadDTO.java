package com.uet.ticketrush.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SeatingPayloadDTO {
    private String sectionLabel;
    private Integer seatsPerRow;
    private List<RowConfigDTO> rowConfigs;
}