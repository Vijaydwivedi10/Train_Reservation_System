import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.OutputStreamWriter;
import java.io.*;
import java.util.Random;
import java.math.BigInteger;
import java.sql.*;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;

class QueryRunner implements Runnable {

    protected Socket socketConnection;
    // connection 

    public QueryRunner(Socket clientSocket) {
        this.socketConnection = clientSocket;
    }

    public static int book(String clientCommand, PrintWriter printWriter, Connection connection) {
        String responseQuery = "";
        try {
            StringTokenizer st = new StringTokenizer(clientCommand, ", ");
            // tokenizing the input

            int num = Integer.parseInt(st.nextToken());

            String[] passengers;
            passengers = new String[num];

            for (int i = 0; i < num; i++) {
                passengers[i] = st.nextToken();
            }
            int train_id = Integer.parseInt(st.nextToken());
            String date = st.nextToken();
            String coach_type = st.nextToken();

            boolean available = false;
            int book_a = 0, avail_a = 0;
            int book_s = 0, avail_s = 0;

            String q1 = "BEGIN;";
            Statement stmt = connection.createStatement();
            stmt.execute(q1);
            // checking whether the choosen berth is AC or SL
            if (coach_type.matches("AC")) {
                String query_a_b = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                        + train_id + " AND trains.doj = '" + date
                        + "' AND booked=1 AND trains.coach_type like 'AC' GROUP BY booked), 0 )";
                String query_a_a = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                        + train_id + " AND trains.doj = '" + date
                        + "' AND booked=0 AND trains.coach_type like 'AC' GROUP BY booked), 0 )";

                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query_a_a);

                while (rs.next()) {
                    avail_a = rs.getInt(1);
                }

                rs = stmt.executeQuery(query_a_b);
                while (rs.next()) {
                    book_a = rs.getInt(1);
                }

                if (avail_a >= num) {
                    available = true;

                } else {

                }
            }

            else {

                String query_s_b = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                        + train_id + " AND trains.doj = '" + date
                        + "' AND booked=1 AND trains.coach_type like  'SL' GROUP BY booked), 0 )";
                String query_s_a = "select coalesce( (SELECT count(*) FROM trains WHERE trains.train_id = "
                        + train_id + " AND trains.doj = '" + date
                        + "' AND booked=0 AND trains.coach_type like 'SL' GROUP BY booked), 0 )";

                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query_s_a);

                while (rs.next()) {
                    avail_s = rs.getInt(1);
                }

                rs = stmt.executeQuery(query_s_b);
                while (rs.next()) {
                    book_s = rs.getInt(1);
                }

                if (avail_s >= num) {
                    available = true;

                } else {

                }
            }


            // Generating a unique PNR number 

            BigInteger pnr = new BigInteger("0");
            if (available == true) {
                Map<String, Integer> unique_pnr = new HashMap<String, Integer>();

                unique_pnr.put("0", 1);
                while (unique_pnr.containsKey(pnr.toString())) {
                    BigInteger maxLimit = new BigInteger("500000000000000");
                    BigInteger minLimit = new BigInteger("2500000000000");
                    BigInteger bigInteger = maxLimit.subtract(minLimit);
                    Random randNum = new Random();
                    int len = maxLimit.bitLength();
                    pnr = new BigInteger(len, randNum);
                    if (pnr.compareTo(minLimit) < 0) {
                        pnr = pnr.add(minLimit);
                    }
                    if (pnr.compareTo(bigInteger) >= 0) {
                        pnr = pnr.mod(bigInteger).add(minLimit);
                    }

                }


//  ----------------------------------------------------------

                unique_pnr.put(pnr.toString(), 1);

                String query_call_book = "";

                String pass = Arrays.toString(passengers);
                pass = pass.replace('[', '{');
                pass = pass.replace(']', '}');

                if (coach_type.matches("AC")) {
                    query_call_book = "call booking_ticket(" + pnr + "," + num + ",'" + pass + "'," + train_id + ",'"
                            + date + "','" + coach_type + "'," + avail_a + "," + book_a + ");";

                } else {
                    query_call_book = "call booking_ticket(" + pnr + "," + num + ",'" + pass + "'," + train_id + ",'"
                            + date + "','" + coach_type + "'," + avail_s + "," + book_s + ");";
                }

                stmt = connection.createStatement();
                stmt.execute(query_call_book);
                String q2 = "COMMIT;";
                stmt.execute(q2);

            }

            else {
                // if ticket are not available
                printWriter.println("Tickets are not available ...");
            }

            String nw_str = pnr.toString();
            String booked_tickets = "SELECT * FROM ticket WHERE pnr = " + nw_str;

            ArrayList<String> responses = new ArrayList<String>();

            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(booked_tickets);

            rs.next();
            while (rs.next()) {
                int coach_no = rs.getInt("coach_no");
                int berth_no = rs.getInt("berth_no");
                String berth_type = rs.getString("berth_type");
                String res_string = "PNR -> " + nw_str + ", " + " Date -> " + date + ", " + " Coach_no -> " + coach_no
                        + ", " + " Coach_type -> " + coach_type + ", " + " Berth_no -> " + berth_no + ", "
                        + " Berth_type -> " + berth_type;
                printWriter.println(res_string);
            }

            responseQuery = String.join("\n", responses);

            return 1;

        } catch (Exception e) {
            return -1;
        }

    }

    public void run() {

        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "postgres", "1212");
            if (connection != null) {
                System.out.println("connection OK");
            } else {
                System.out.println("connection FAILED");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            // Reading data from client
            InputStreamReader inputStream = new InputStreamReader(socketConnection
                    .getInputStream());
            BufferedReader bufferedInput = new BufferedReader(inputStream);
            OutputStreamWriter outputStream = new OutputStreamWriter(socketConnection
                    .getOutputStream());
            BufferedWriter bufferedOutput = new BufferedWriter(outputStream);
            PrintWriter printWriter = new PrintWriter(bufferedOutput, true);
            String clientCommand = "";

            clientCommand = bufferedInput.readLine();
            while (!clientCommand.equals("#")) {

                while (book(clientCommand, printWriter, connection) == -1)
                    ;
                clientCommand = bufferedInput.readLine();
            }
            inputStream.close();
            bufferedInput.close();
            outputStream.close();
            bufferedOutput.close();
            printWriter.close();
            socketConnection.close();
        } catch (IOException e) {
            return;
        }
    }
}

public class ServiceModule {

    static int serverPort = 7008;

    static int numServerCores = 5;

    public static void main(String[] args) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(numServerCores);

        try (
                ServerSocket serverSocket = new ServerSocket(serverPort)) {
            Socket socketConnection = null;

            while (true) {
                System.out.println("Listening port : " + serverPort + "\nWaiting for clients...");
                System.out.println("working");
                socketConnection = serverSocket.accept();
                System.out.println("Accepted client :"
                        + socketConnection.getRemoteSocketAddress().toString()
                        + "\n");

                Runnable runnableTask = new QueryRunner(socketConnection);

                executorService.submit(runnableTask);
            }
        }
    }
}