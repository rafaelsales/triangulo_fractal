package com.rafaelsales.triangulofractal.fractal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Administrador
 */
public class Atrator {

    
    public enum Forma {
        Ponto,
        Circulo,
        Quadrado
    }
    
    private Forma forma;
    private boolean movel;
    private short atuacao;

    private Point2D localizacao;
    private Point2D localizacaoInicial;
    private Ellipse2D ponto;
    private Ellipse2D circulo;
    private Rectangle quadrado;
    private int raioLado;
    private int deslocamentoMaximo;
    private double deslocamento = 0;
    
    public Atrator(Forma forma, boolean movel, short atuacao) {
        this.forma = forma;
        this.movel = movel;
        this.atuacao = atuacao;
    }
    
    public Forma getForma() {
        return forma;
    }

    public void setForma(Forma forma) {
        this.forma = forma;
    }

    public boolean isMovel() {
        return movel;
    }

    public void setMovel(boolean movel) {
        this.movel = movel;
    }
    
    public Point2D getLocalizacao(){
        return this.localizacao;
    }
    
    public void setLocalizacao(Point2D localizacao) {
        this.localizacao = localizacao;
        fixarEstado();
    }
    
    public int getRaioLado() {
        return raioLado;
    }
    
    public void setRaioLado(int raioLado) {
        this.raioLado = raioLado;
    }

    public short getAtuacao() {
        return atuacao;
    }
        
    public void setLocalizacaoInicial(Point2D localizacaoInicial) {
        this.localizacaoInicial = localizacaoInicial;
    }
    
    public Point getPontoVariante() {
        if (forma == Forma.Ponto)
            return new Point((int)getLocalizacao().getX(), (int)getLocalizacao().getY());
        
        //Se for Circulo ou Quadrado:
        int x = 0, y = 0;
        switch (forma) {
            case Circulo:
                x = (int)(Math.random() * circulo.getWidth() + circulo.getX());
                y = (int)(Math.random() * circulo.getHeight() + circulo.getY());
                break;
            case Quadrado:
                x = (int)(Math.random() * quadrado.getWidth() + quadrado.getX());
                y = (int)(Math.random() * quadrado.getHeight() + quadrado.getY());
                break;
        }

        return new Point(x, y);
    }
    
    public Point getCentro(){
        Point pCentro = new Point();
        
        switch (forma) {
            case Circulo:
                pCentro.x = (int)circulo.getCenterX();
                pCentro.y = (int)circulo.getCenterY();
                break;
            case Quadrado:
                pCentro.x = (int)quadrado.getCenterX();
                pCentro.y = (int)quadrado.getCenterY();
                break;
            case Ponto:
                pCentro.x = (int)ponto.getCenterX();
                pCentro.y = (int)ponto.getCenterY();
                break;
        }
        return pCentro;
    }

    public void setDeslocamentoMaximo(int deslocamentoMaximo) {
        this.deslocamentoMaximo = deslocamentoMaximo;
    }
    
    public void deslocar(double x, double y) {
        if (deslocamento > deslocamentoMaximo)
            return;
        
        setLocalizacao(new Point2D.Double(localizacao.getX() - x, localizacao.getY() - y));
        deslocamento += Math.abs(x);
    }

    public boolean contemPonto(Point ponto) {
        boolean result = false;
        switch (forma) {
            case Circulo:
                result = circulo.contains(ponto);
                break;
            case Quadrado:
                result = quadrado.contains(ponto);
                break;
            case Ponto:
                result = this.ponto.contains(ponto);
                break;
        }
        return result;
    }
    
    public void limparUso() {
        this.deslocamento = 0;
        localizacao = localizacaoInicial;
    }
    
    public void fixarEstado() {
        if (forma == Forma.Ponto) {
            quadrado = null;
            circulo = null;
            ponto = new Ellipse2D.Float(
                    (float)(localizacao.getX() - 4),
                    (float)(localizacao.getY() - 4),
                    8,
                    8);
        } else if (forma == Forma.Circulo) {
            ponto = null;
            quadrado = null;
            circulo = new Ellipse2D.Float(
                    (float)(localizacao.getX() - raioLado),
                    (float)(localizacao.getY() - raioLado),
                    raioLado*2,
                    raioLado*2);
        } else if (forma == Forma.Quadrado) {
            ponto = null;
            circulo = null;
            quadrado = new Rectangle(
                    (int)(localizacao.getX() - raioLado),
                    (int)(localizacao.getY() - raioLado),
                    raioLado*2,
                    raioLado*2);
        }
    }
    
    public void render(Graphics2D g) {
        Color azul = new Color(20, 20, 200);
        if (localizacao == null)
            return;
        if (forma == Forma.Ponto){
            g.setColor(azul);
            g.fill(ponto);
            g.draw(ponto);
        } else if (forma == Forma.Circulo) {
            //g.setColor(new Color(240, 240, 240));
            //g.fill(circulo);
            g.setColor(azul);
            g.draw(circulo);
        } else if (forma == Forma.Quadrado) {
            //g.setColor(new Color(240, 240, 240));
            //g.fill(quadrado);
            g.setColor(azul);
            g.draw(quadrado);
        }
    }
}
