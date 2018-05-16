package com.ramon.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.ramon.cursomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);
	void sendEmail(SimpleMailMessage msg);
}
