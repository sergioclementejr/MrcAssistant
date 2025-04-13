package com.mchouse.mcrassistant.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ControleCondominio {

    /**
     * Coluna "Imóvel Locado"
     */
    private String imovelLocado;
    /**
     * Coluna "Imóvel"
     */
    private String imovel;
    /**
     * Coluna "Locatário"
     */
    private String locatario;
    /**
     * Coluna "Locador"
     */
    private String locador;
    /**
     * Coluna "email"
     */
    private String email;
    /**
     * Coluna "adm"
     */
    private String adm;
    /**
     * Coluna "envio"
     */
    private boolean enviado;
    /**
     * Coluna "retorno"
     */
    private boolean retorno;

    private boolean validado;


    public boolean isInvalido(){
        return !isValidado();
    }
}
