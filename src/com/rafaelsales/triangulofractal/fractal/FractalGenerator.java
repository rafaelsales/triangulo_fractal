package com.rafaelsales.triangulofractal.fractal;

import com.rafaelsales.triangulofractal.TrianguloFractalView;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrador
 */
public class FractalGenerator implements MouseMotionListener, MouseListener, Runnable {

    public enum Cor {
        Vermelho, Verde, Azul, Amarelo
    }
    
    //Lista de atratores já adicionados na tela:
    private List<Atrator> atratores = new ArrayList<Atrator>();
    //Usado enquanto o usuario ainda não definiu a posicao do atrator:
    private Atrator atratorTemp;
    
    //Ponto de referencia para gerar o fractal. Usada na geração do fractal:
    private Point referencia;
    private Ellipse2D referenciaEllipse;
    //Referencia da tela:
    private TrianguloFractalView view;
    //Area de desenho:
    private Canvas canvas;
    //Fractal:
    private BufferedImage imagem;
    
    //=== Double Buffering
    private Image imgDB;
    private Graphics2D gDB;
    private final Rectangle retanguloTela = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    //=== Double Buffering
    
    private Thread thread;
    private final long REFRESH_NORMAL = 500l;
    private final long REFRESH_GERANDO = 100l;
    private long refreshRate = REFRESH_NORMAL;

    
    public FractalGenerator(final TrianguloFractalView view) {
        this.view = view;
        this.canvas = view.getCanvas();
        thread = new Thread(this);
        
        new Thread(new Runnable() {
            public void run() {
                while (!view.getFrame().isDisplayable()) {
                    try {
                        Thread.sleep(500L);
                        Thread.yield();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FractalGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                canvas.getGraphics().clearRect(0, 0, retanguloTela.width, retanguloTela.height);
                iniciarDoubleBuffer();
                thread.start();
            }
        }).start();
        
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
    }
    
    public List<Atrator> getAtratores() {
        return atratores;
    }
    
    public synchronized void criarAtrator(Atrator.Forma forma, boolean movel, int atuacao, int tamanho) {
        atratorTemp = new Atrator(forma, movel, (short)atuacao);
        if (forma != Atrator.Forma.Ponto)
            atratorTemp.setRaioLado(tamanho);
    }
    
    public synchronized void criarReferencia() {
        referenciaEllipse = new Ellipse2D.Float();
        referencia = null;
    }
    
    public boolean isReferenciaCriada() {
        return referencia != null;
    }
    
    public void gerarFractal(final int numPontos, final Cor cor) {
        new Thread(new Runnable() {

            public void run() {
                if (referencia == null)
                    referencia = new Point(0, 0);
                
                Graphics2D g = imagem.createGraphics();
                Color corDeslocamento = new Color(20, 0, 20);
                Color corPonto = Color.BLACK;
                //Seta a cor dos pontos:
                switch (cor) {
                    case Amarelo:
                        corPonto = Color.YELLOW;
                        break;
                    case Azul:
                        corPonto = Color.BLUE;
                        break;
                    case Vermelho:
                        corPonto = Color.RED;
                        break;
                    case Verde:
                        corPonto = Color.GREEN;
                        break;
                }
                g.setColor(corPonto);
                
                /**Calcula o centro dos triangulos que servirá de referencia
                 * para o deslocamento de atratores móveis.
                 */
                Point centro = new Point(0,0);
                for (Atrator atrator : atratores) {
                    centro.x += atrator.getCentro().x;
                    centro.y += atrator.getCentro().y;
                }
                centro.x = (int)(centro.x * 1d)/atratores.size();
                centro.y = (int)(centro.y * 1d)/atratores.size();
                
                //Seta a distância maxima de deslocamento dos atratores moveis:
                for (Atrator atrator : atratores) {
                    //Distancia máxima: Metade da distancia do atrator ao centro do fractal gerado:
                    if (atrator.isMovel())
                        atrator.setDeslocamentoMaximo((int)(centro.distance(atrator.getCentro()) / 2));
                }
                
                //===INICIO DA GERAÇÃO DO FRACTAL:
                Atrator atrator = null;
                Point pMedio;
                
                view.ativarBotoes(false);
                view.getProgresso().setValue(0);
                Point locAtrator;
                int diferencaX;
                int diferencaY;
                boolean atratorEscolhido;
                
                //Velocidade de deslocamento dos atratores moveis:
                float velocDesloc = 900f/numPontos;
                
                //Aumenta a taxa de refresh:
                refreshRate = REFRESH_GERANDO;
                //Itera pelo numero de pontos do fractal:        
                for (int i = 0; i < numPontos; i++) {
                    //Atualiza a barra de progresso:
                    if (i % 1000 == 0)
                        view.getProgresso().setValue((int)(100.0/numPontos * i));
                    
                    //Pega um atrator aleatoriamente:
                    atratorEscolhido = false;
                    while (!atratorEscolhido) {
                        //Escolhe um atrator aleatoriamente:
                        atrator = atratores.get((int)(Math.random() * atratores.size()));
                        //Filtra a escolha do atrator de acordo com a porcentagem de atuação:
                        if (atrator.getAtuacao() / 100f >= Math.random())
                            atratorEscolhido = true;
                    }
                    
                    //Calcula o ponto médio entre o atrator e a referencia:
                    if (atrator.getForma() == Atrator.Forma.Ponto)
                        locAtrator = new Point((int)atrator.getLocalizacao().getX(), (int)atrator.getLocalizacao().getY());                    
                    else
                        locAtrator = atrator.getPontoVariante();
                    
                    diferencaX = locAtrator.x - referencia.x;
                    diferencaY = locAtrator.y - referencia.y;
                    pMedio = new Point(
                            (int)(locAtrator.x - (diferencaX)/2.0),
                            (int)(locAtrator.y - (diferencaY)/2.0)
                            );

                    //Se o atrator for móvel, faz seu deslocamento:
                    if (atrator.isMovel()) {
                        g.setColor(corDeslocamento);
                        g.drawRect((int)atrator.getCentro().getX(), (int)atrator.getCentro().getY(), 0, 0);
                        g.setColor(corPonto);
                        atrator.deslocar((diferencaX > 0) ? velocDesloc : -velocDesloc, (diferencaY > 0) ? velocDesloc : -velocDesloc);
                    }
                    
                    //Agora, a referencia é o ponto medio entre a referencia antiga e o atrator:
                    referencia = pMedio;
                    
                    //Desenha o ponto:
                    g.drawRect(pMedio.x, pMedio.y, 0, 0);
                    
                    //A cada porção de pontos calculados, libera prioridade para outras threads:
                    if (i % 15000 == 0) {
                        Thread.yield();
                    }
                }
                refreshRate = REFRESH_NORMAL; 
                for (Atrator a : atratores)
                    a.limparUso();

                view.getProgresso().setValue(100);
                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException ex) {
                }
                view.getProgresso().setValue(0);
                view.ativarBotoes(true);
            }
        }).start();
        
    }
        
    private synchronized void fixarAtrator(){
        atratorTemp.fixarEstado();
        atratores.add(this.atratorTemp);
        atratorTemp = null;
    }
    private synchronized void descartarAtrator(){
        this.atratorTemp = null;
    }
    
    public synchronized void iniciarDoubleBuffer(){
        imgDB = canvas.createImage(retanguloTela.width, retanguloTela.height);
        gDB = (Graphics2D) imgDB.getGraphics();

        imagem = new BufferedImage(retanguloTela.width, retanguloTela.height, BufferedImage.TYPE_INT_ARGB);
    }
    
    public synchronized void render(){
        if (gDB == null)
            return;    
        gDB.clearRect(0, 0, retanguloTela.width, retanguloTela.height);
              
        if (imagem != null)
            gDB.drawImage(imagem, 0, 0, canvas);
        
        if (atratorTemp != null) {
            atratorTemp.render(gDB);
        }
        
        for (Atrator atrator : atratores){
            atrator.render(gDB);            
        }
        
        if (referenciaEllipse != null) {
            gDB.setColor(Color.DARK_GRAY);
            gDB.fill(referenciaEllipse);
        }
        
        canvas.getGraphics().drawImage(imgDB, 0, 0, canvas);
    }
    
    public void run() {
        while (true) {
            render();
            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException ex) {
                Logger.getLogger(FractalGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void limparImagem(){
        iniciarDoubleBuffer();
        for(Atrator atrator : atratores) {
            atrator.limparUso();
            atrator.fixarEstado();
        }
    }

    public synchronized void finalizar() {
        canvas.removeMouseListener(this);
        canvas.removeMouseMotionListener(this);
        
        atratores.clear();
        atratorTemp = null;
        referencia = null;
        view = null;
        canvas = null;
        imagem = null;
        gDB.dispose();
        gDB = null;
        imgDB = null;
    }
    
    //=========== EVENTOS DO MOUSE ===========
    
    public void mouseDragged(MouseEvent e) {
        
    }

    public void mouseMoved(MouseEvent e) {
        if (atratorTemp != null) {
            atratorTemp.setLocalizacao(e.getPoint());
            atratorTemp.fixarEstado();
            render();
        }
        if (referencia == null && referenciaEllipse != null) {
            referenciaEllipse = new Ellipse2D.Float(e.getX() - 3, e.getY() - 3, 7, 7);
            render();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        if (atratorTemp != null) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                atratorTemp.setLocalizacao(e.getPoint());
                atratorTemp.setLocalizacaoInicial(e.getPoint());
                this.fixarAtrator();
            }
            else
                this.descartarAtrator();
        } else {
            if (e.getButton() == MouseEvent.BUTTON3){
                ListIterator<Atrator> atratorIterator = atratores.listIterator();
                while (atratorIterator.hasNext()) {
                    Atrator atrator = atratorIterator.next();
                    if (atrator.contemPonto(e.getPoint())) {
                        atratorIterator.remove();
                        break;
                    }
                }
            }
        }
        if (referencia == null && referenciaEllipse != null) {
            referencia = new Point((int)referenciaEllipse.getCenterX(), (int)referenciaEllipse.getCenterY());
        }
        render();
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }
}
