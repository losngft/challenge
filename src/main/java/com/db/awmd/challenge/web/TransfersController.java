package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
import com.db.awmd.challenge.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class TransfersController {

    private final TransferService transferService;

    @Autowired
    public TransfersController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTransfer(@RequestBody @Valid Transfer transfer) {
        log.info("Creating transfer {}", transfer);

        try {
            this.transferService.createTransfer(transfer);
        } catch (NegativeBalanceException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (
                InvalidAmountException invalidAmountException) {
            return new ResponseEntity<>(invalidAmountException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (
                NonexistentAccountException nonexistentAccountException) {
            return new ResponseEntity<>(nonexistentAccountException.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}
