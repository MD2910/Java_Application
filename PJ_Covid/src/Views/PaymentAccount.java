package Views;

import CONNECT.JDBC;
import QUANLY.DuNo;
import QUANLY.GOINHUYEUPHAM;
import Service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.awt.*;

public class PaymentAccount extends JFrame{
    private JPanel payment;
    private JTable infoPayment;
    private JButton muaButton;
    private JButton nạpTiềnButton;
    private JButton closeButton;
    private JTextField money;
    private JTable infoGNYP;
    private JTextField Goimua;
    private JButton nợButton;
    private JButton thanhToánNợButton;
    DefaultTableModel model;

    PaymentService paymentService = new PaymentService();
    public void showPaymentAcc(){
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setVisible(true);
        paymentAccount.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        paymentAccount.setLocationRelativeTo(null);
//        paymentAccount.pack();
    }
    public PaymentAccount(){
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(payment);
//        this.setLocationRelativeTo(null);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                HomePage homePage = new HomePage();
                homePage.showHomePage();
            }
        });

        nạpTiềnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    new PaymentService().napTienbyID(LoginPage.CCCD,Integer.parseInt(money.getText()) );
                    dispose();
                    PaymentAccount paymentAccount = new PaymentAccount();
                    paymentAccount.showPaymentAcc();
            }
        });
        muaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(new PaymentService().gethanChe(LoginPage.CCCD).equals("No")){
                    if(new NhuyeuphamService().getGioiHanMua(Goimua.getText()) > 0){
                        String[] listNYP = Goimua.getText().split(",");
                        NhuyeuphamService nhuyeuphamService = new NhuyeuphamService();
                        float tongtien = 0;
                        for(String nyp : listNYP){
                            tongtien += nhuyeuphamService.tinhThanhTien(nyp);
                        }
                        if(tongtien <  paymentService.getSoduTKbyID(LoginPage.CCCD)){
                            new PaymentService().muaHang(tongtien,LoginPage.CCCD);
                            new PaymentService().thanhtoanAdmin(tongtien);
                        }
                        else{
                            JOptionPane.showMessageDialog(null,"So du tai khoan khong du de mua goi hang nay ! Vui long nap them tien");
                        }


                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                        String strDate = formatter.format(date);
                        LSMHService lsmhService = new LSMHService();
                        lsmhService.thaoTac(LoginPage.CCCD , strDate , Goimua.getText());
                        new NhuyeuphamService().upDateGH(Goimua.getText());
                        dispose();
                        PaymentAccount  paymentAccount = new PaymentAccount();
                        paymentAccount.showPaymentAcc();
                    }
                    else JOptionPane.showMessageDialog(null,"Gói sản phẩm này đã hết hàng !!!");
                }
                else{
                    JOptionPane.showMessageDialog(null,"Tài khoản của bạn hiện không thực hiện được thao tác này " +
                            "Vui lòng thanh toán nợ để có thể mua hàng");
                }

            }
        });
        nợButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(new DuNoService().checkDuNoTrong(LoginPage.CCCD) == true){
                    String[] listNYP = Goimua.getText().split(",");
                    NhuyeuphamService nhuyeuphamService = new NhuyeuphamService();
                    float tongtien = 0;
                    for(String nyp : listNYP){
                        tongtien += nhuyeuphamService.tinhThanhTien(nyp);
                    }
                    PaymentService paymentService = new PaymentService();
                    PaymentService paymentService1 = new PaymentService();
                    float SotienNo = paymentService1.getSoNobyID(LoginPage.CCCD) + tongtien;
                    paymentService.updateGhiNo(LoginPage.CCCD , SotienNo );
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                    String strDate = formatter.format(date);
                    DuNoService duNoService = new DuNoService();
                    duNoService.thaoTac(LoginPage.CCCD,strDate,Goimua.getText());
                    dispose();
                    PaymentAccount paymentAccount = new PaymentAccount();
                    paymentAccount.showPaymentAcc();
                }
                else {
                    JOptionPane.showMessageDialog(null,"Bạn chưa thanh toán gói nợ cũ ");
                }
            }
        });
        thanhToánNợButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    float tienHienCo = new PaymentService().getSoduTKbyID(LoginPage.CCCD);
                   float tienNo = new PaymentService().getSoNobyID(LoginPage.CCCD) ;
                   if(tienHienCo >= tienNo){
                      new PaymentService().thanhtoanNo(LoginPage.CCCD);
                       new NhuyeuphamService().upDateGH(Goimua.getText());

                   }
                   else{
                       JOptionPane.showMessageDialog(null,"Số tiền hiện tại không đủ để thanh toán");
                   }

                   dispose();
                   PaymentAccount paymentAccount = new PaymentAccount();
                   paymentAccount.showPaymentAcc();
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
          String header[] ={"Id" , " Số dư tài khoản " , " Số nợ " };
           model = new DefaultTableModel(0,8);
           model.setColumnIdentifiers(header);
           infoPayment = new JTable(model);
           try {
               Connection connection = JDBC.connect();
               Statement statement = connection.createStatement();
               String sql = "select * from COVID.payment " ;
               ResultSet resultSet  = statement.executeQuery(sql);
               while(resultSet.next()){
                   if(!resultSet.getString("CCCD").equals("admin")){
                       String id = resultSet.getString("CCCD");
                       int soduHT = resultSet.getInt("SoduTK");
                       int  soNo  = resultSet.getInt("SoNo");
                       Object [] tbData  = {id,soduHT,soNo};
                       model.addRow(tbData);
                   }
               }
           }
           catch (Exception exception){
               exception.getMessage();
           }
        NhuyeuphamService nhuyeuphamService = new NhuyeuphamService();
        String header1[] ={"Tên Gói" , "Giới hạn mua" , "Ngày hết hạn", "Thành tiền"};
        model = new DefaultTableModel(0,4);
        model.setColumnIdentifiers(header1);
        infoGNYP = new JTable(model);
        List<GOINHUYEUPHAM> dsNYP;
        dsNYP = nhuyeuphamService.getAllNhuYeuPham();

        for (GOINHUYEUPHAM goinhuyeupham: dsNYP){
            float thanhtien  = nhuyeuphamService.tinhThanhTien(goinhuyeupham.getTENGOI());
            model.addRow(new Object[]{goinhuyeupham.getTENGOI(), goinhuyeupham.getMUCGIOIHAN(), goinhuyeupham.getNGAYHETHAN(),thanhtien});
        }
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(infoPayment.getModel());
        infoPayment.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        infoPayment.setRowHeight(50);
        infoGNYP.setRowHeight(50);
        muaButton = new JButton(new ImageIcon(new ImageIcon("image\\buy.png").getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
        muaButton.setPreferredSize(new Dimension(60, 60));
        nạpTiềnButton = new JButton(new ImageIcon(new ImageIcon("image\\money.png").getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
        nạpTiềnButton.setPreferredSize(new Dimension(60, 60));
        closeButton = new JButton(new ImageIcon(new ImageIcon("image\\back.png").getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
        closeButton.setPreferredSize(new Dimension(60, 60));
        thanhToánNợButton = new JButton(new ImageIcon(new ImageIcon("image\\cash.png").getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
        thanhToánNợButton.setPreferredSize(new Dimension(60, 60));
        nợButton = new JButton(new ImageIcon(new ImageIcon("image\\debt.png").getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
        nợButton.setPreferredSize(new Dimension(60, 60));

    }

}
