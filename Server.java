import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;

public class Server {

  private int port;
  private List<User> clients;
  private ServerSocket server;

  public static void main(String[] args) throws IOException {
    new Server(5555).run();
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();
  }

  public void run() throws IOException {
    server = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    System.out.println("A porta 5555 está agora aberta.");

    while (true) {
      // aceita um novo cliente
      Socket client = server.accept();

      // obter nome do novo utilizador
      String nickname = (new Scanner(client.getInputStream())).nextLine();
      nickname = nickname.replace(",", ""); // ',' usar para serialização
      nickname = nickname.replace(" ", "_");
      System.out
          .println("Novo cliente: \"" + nickname + "\"\n\t     Servidor:" + client.getInetAddress().getHostAddress());

      // criar novo utilizador
      User newUser = new User(client, nickname);

      // adicionar nova mensagem do utilizador à lista
      this.clients.add(newUser);

      // Mensagem de boas vindas
      newUser.getOutStream().println(
          "<center><img src='https://media.tenor.com/wPudCfjCrD8AAAAM/penguin-hello.gif' height='45' width='45'> "
              + " <b>Bem-vindo</b>" + newUser.toString() +
              " <img src='https://media.tenor.com/wPudCfjCrD8AAAAM/penguin-hello.gif' height='45' width='45'></center>"
              + "<br><hr style = \" border: dashed 2px ;\"></hr>");

      // crie um novo tópico para o tratamento de novas mensagens recebidas do
      // utilizador
      new Thread(new UserHandler(this, newUser)).start();
    }
  }

  // excluir um utilizador da lista
  public void removeUser(User user) {
    this.clients.remove(user);
  }

  // enviar mensagem recebida para todos os utilizadores
  public void broadcastMessages(String msg, User userSender) {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.toString() + "<span>: " + msg + "</span>");
    }
  }

  // enviar lista de clientes para todos os utilizadores
  public void broadcastAllUsers() {
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // enviar mensagem para um utilizador (String)
  public void sendMessageToUser(String msg, User userSender, String user) {
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() + ": " + msg);
        client.getOutStream().println(
            "(<b>Privado</b>)" + userSender.toString() + "<span>: " + msg + "</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toString() + " -> (<b>ninguém!</b>): " + msg);
    }
  }
}

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // quando houver uma nova mensagem, enviar para todos
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();

      // emojis
      message = message.replace(":)",
          "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
      message = message.replace(":D",
          "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":d",
          "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":(",
          "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
      message = message.replace("-_-",
          "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
      message = message.replace(";)",
          "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
      message = message.replace(":P",
          "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":p",
          "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":o",
          "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
      message = message.replace(":O",
          "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");

      // Gestão das mensagens privadas
      if (message.charAt(0) == '@') {
        if (message.contains(" ")) {
          System.out.println("mensagem privada : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate = message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                  firstSpace + 1, message.length()),
              user, userPrivate);
        }

        // Mudar a cor
      } else if (message.charAt(0) == '#') {
        user.changeColor(message);
        // atualizar cor para todos os outros utilizadores
        this.server.broadcastAllUsers();
      } else {
        // atualizar lista de utilizadores
        server.broadcastMessages(message, user);
      }
    }
    // fim do tópico
    server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;
  private String color;

  // Método construtor
  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // alterar cor do utilizador
  public void changeColor(String hexColor) {
    // verifique se é uma cor hexadecimal válida
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()) {
      Color c = Color.decode(hexColor);
      // se a cor for muito brilhante, não mude!!!
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // por ITU-R BT.709
      if (luma > 160) {
        this.getOutStream().println("<b>Cor muito brilhante</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Cor alterada com sucesso</b> " + this.toString());
      return;
    }
    this.getOutStream().println("<b>Falha ao alterar a cor</b>");
  }

  // logs
  public PrintStream getOutStream() {
    return this.streamOut;
  }

  public InputStream getInputStream() {
    return this.streamIn;
  }

  public String getNickname() {
    return this.nickname;
  }

  // imprimir utilizador com a sua cor
  public String toString() {

    return "<u><span style='color:" + this.color
        + "'>" + this.getNickname() + "</span></u>";

  }
}

class ColorInt {
  public static String[] mColors = {
      "#3079ab", // dark blue
      "#e15258", // red
      "#f9845b", // orange
      "#7d669e", // purple
      "#53bbb4", // aqua
      "#51b46d", // green
      "#e0ab18", // mustard
      "#f092b0", // pink
      "#e8d174", // yellow
      "#e39e54", // orange
      "#d64d4d", // red
      "#4d7358", // green
  };

  public static String getColor(int i) {
    return mColors[i % mColors.length];
  }
}