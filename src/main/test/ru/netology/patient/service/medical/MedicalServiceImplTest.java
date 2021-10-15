package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MedicalServiceImplTest {
    String message;
    PatientInfo patientInfo;
    SendAlertService alertService;
    MedicalService medicalService;
    @BeforeEach
    void setUp() {
        //given
        message = "Warning, patient with id: 1234, need help";
        PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        patientInfo = new PatientInfo("1234","Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
        Mockito.when(patientInfoRepository.add(patientInfo))
                .thenReturn("1234");
        Mockito.when(patientInfoRepository.getById("1234"))
                .thenReturn(patientInfo);
        alertService = Mockito.mock(SendAlertServiceImpl.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @MethodSource("factory")
    @DisplayName("Check Blood Pressure Warning Test")
    void checkBloodPressureWarning(BloodPressure bloodPressure, BigDecimal temperature) {
        //when
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        medicalService.checkBloodPressure(patientInfo.getId(), bloodPressure);
        //then
        Mockito.verify(alertService, Mockito.atLeast(1)).send(argumentCaptor.capture());
        Assertions.assertEquals(message, argumentCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("factory")
    void checkTemperature(BloodPressure bloodPressure, BigDecimal temperature) {
        //when
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        medicalService.checkTemperature("1234", temperature);
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals(message, argumentCaptor.getValue());
    }

    @Test
    @DisplayName("No Message Is Sent When Blood Pressure Is Same")
    void noMessageBloodPressureSent(){
        //given
        BloodPressure bloodPressure = new BloodPressure(120, 80);
        BigDecimal temperature = new BigDecimal("36.6");
        //when
        medicalService.checkBloodPressure("1234", bloodPressure);
        //then
        Mockito.verify(alertService, Mockito.never()).send(message);
    }

    @Test
    @DisplayName("No Message Is Sent When Temperature The Is Same")
    void noMessageTemperatureSent(){
        //given
        BigDecimal temperature = new BigDecimal("36.6");
        //when
        medicalService.checkTemperature("1234", temperature);
        //then
        Mockito.verify(alertService, Mockito.never()).send(message);
    }

    public static Stream<Arguments> factory (){
        return Stream.of(
                Arguments.of(new BloodPressure(60, 120), new BigDecimal("1.5")),
                Arguments.of(new BloodPressure(140, 60), new BigDecimal("2.2"))
        );
    }
}