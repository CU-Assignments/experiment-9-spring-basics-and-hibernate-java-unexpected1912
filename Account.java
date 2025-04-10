import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import javax.persistence.*;
import java.util.Date;

@Entity
class Account {
    @Id private int accountId;
    private String name;
    private double balance;

    public Account() {}
    public Account(int id, String name, double balance) {
        this.accountId = id; this.name = name; this.balance = balance;
    }
    public int getAccountId() { return accountId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}

@Entity
class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    private int fromAccount, toAccount;
    private double amount;
    private Date timestamp;

    public Transfer() {}
    public Transfer(int from, int to, double amount) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.timestamp = new Date();
    }
}

public class App {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Account.class)
                .addAnnotatedClass(Transfer.class)
                .buildSessionFactory();

        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Account from = session.get(Account.class, 101);
            Account to = session.get(Account.class, 102);
            double amount = 300;

            if (from.getBalance() < amount) {
                throw new RuntimeException("Insufficient balance");
            }

            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);

            session.update(from);
            session.update(to);
            session.save(new Transfer(from.getAccountId(), to.getAccountId(), amount));

            tx.commit();
            System.out.println("Transfer successful");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Transfer failed: " + e.getMessage());
        } finally {
            session.close();
            factory.close();
        }
    }
}