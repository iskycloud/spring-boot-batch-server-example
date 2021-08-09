package com.example.springbatch.batch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Configuration
public class SimpleJobBatchConfiguration {

    @NonNull
    private final StepBuilderFactory stepBuilderFactory;
    @NonNull
    private final JobBuilderFactory jobBuilderFactory;
    @NonNull
    private final JobExplorer jobExplorer;
    @NonNull
    private final JobLauncher jobLauncher;

    @Bean
    protected Step stepFirst() {
        return stepBuilderFactory
                .get("stepFirst")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step1");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Job jobExample() {
        return jobBuilderFactory
                .get("jobExample")
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .start(stepFirst())
                .build();
    }


    @Scheduled(cron = "0 50 * * * ?")
    public void scheduledRunJobExample() {
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(jobExample()).toJobParameters();
        try {
            jobLauncher.run(jobExample(), jobParameters);
        } catch (Exception e) {

        }
    }

    @GetMapping(value = "/test")
    public String apiRunJobExample() {
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(jobExample()).toJobParameters();
        try {
            jobLauncher.run(jobExample(), jobParameters);
        } catch (Exception e) {

        }
        return "OK";
    }

}
