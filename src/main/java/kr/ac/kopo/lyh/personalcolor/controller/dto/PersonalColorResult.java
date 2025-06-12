package kr.ac.kopo.lyh.personalcolor.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalColorResult {
    private String prediction;
    private double confidence;
    private List<String> recommendations;
}
