import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.StringReader;

public class Client {

  private String host;
  private int port;

  public static void main(String[] args) throws UnknownHostException, IOException {
    new Client("localhost", 5555).run();
  }

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws UnknownHostException, IOException {
    // conectar cliente ao servidor
    Socket client = new Socket(host, port);
    System.out.println("Cliente conectado com sucesso ao servidor!");

    // Obtenha o fluxo de saída do Socket (onde o cliente envia sua mensagem)
    PrintStream output = new PrintStream(client.getOutputStream());

    // pedir um nome
    Scanner sc = new Scanner(System.in);
    System.out.print("Escreva um nome: ");
    String nickname = sc.nextLine();

    // enviar nome para o servidor
    output.println(nickname);

    // crie uma nova thread para manipulação de mensagens do servidor
    new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

    // ler mensagens do teclado e enviar para o servidor
    System.out.println("Mensagens: \n");

    // novas mensagens
    while (sc.hasNextLine()) {
      output.println(sc.nextLine());
    }

    // end ctrl D
    output.close();
    sc.close();
    client.close();
  }
}

class ReceivedMessagesHandler implements Runnable {

  private InputStream server;

  public ReceivedMessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {
    // receber mensagens do servidor e imprimir no ecrã
    Scanner s = new Scanner(server);
    String tmp = "";
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
        System.out.println(
            "\nLISTA DE UTILIZADORES: " +
            new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n"
            );
      }else{
        try {
          System.out.println("\n" + getTagValue(tmp));
        } catch(Exception ignore){}
      }
    }
    s.close();
  }

  // Eu poderia usar um javax.xml.parsers, mas o objetivo do Client.java é manter tudo compacto e simples
  public static String getTagValue(String xml){
    return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
  }

}
