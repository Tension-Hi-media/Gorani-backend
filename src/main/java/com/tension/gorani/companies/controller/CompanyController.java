package com.tension.gorani.companies.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "company")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/company")
public class CompanyController {
}
