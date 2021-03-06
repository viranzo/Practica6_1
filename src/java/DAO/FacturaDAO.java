/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DAO;
import Entidad.Articulo;
import Entidad.Cliente;
import Entidad.Exceptions.ProgException;
import Entidad.Factura;
import Entidad.Utilidad.Log;
import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author victor
 */
public class FacturaDAO {

    public Factura findById(Connection con, Factura factura) throws  Exception {
       Factura factura1=null;
       ResultSet rs=null;
       PreparedStatement stmt=null;        
       try {
            stmt = con.prepareStatement("SELECT * FROM Factura WHERE idFactura=?");
            stmt.setInt(1,factura.getId());
            
            rs =stmt.executeQuery();
            if (rs.next()) {
               factura1=new Factura();
               factura1.setId(rs.getInt("idFactura"));
               factura1.setFecha(rs.getInt("Fecha"));
            }
                         
        } catch (SQLException ex) {
           //ex.printStackTrace();
           Log.getInstance().error(ex); 
           throw new ProgException("Ha habido un problema al buscar la factura "+ex.getMessage());
        }  finally
        {
            if (rs != null) rs.close(); //Cerramos el resulset
            if (stmt != null) stmt.close();//Cerramos el Statement 
        } 
       return factura1;
        
    }
            

    public Factura creaFactura(Connection con, Factura factura,Cliente cliente) throws  Exception
    {
           PreparedStatement stmt=null;
           try {

                stmt = con.prepareStatement("INSERT INTO Factura(Fecha,Cliente_DNI) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1,factura.getFecha());
                stmt.setInt(2,cliente.getDNI());
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys ();
                rs.next();
                int key = rs.getInt(1);
                factura.setId(key);

            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al insertar la factura "+ex.getMessage());
            }finally
            {
                 if (stmt != null) stmt.close();//Cerramos el Statement 
            }
           return factura;
    }

    public Factura borraFactura(Connection con, Factura factura) throws  Exception
    {
           PreparedStatement stmt=null;
           try {

                stmt = con.prepareStatement("DELETE FROM Factura WHERE idFactura=?");
                stmt.setInt(1,factura.getId());
                stmt.executeUpdate();

            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al borrar la factura "+ex.getMessage());
            }finally
            {
                 if (stmt != null) stmt.close();//Cerramos el Statement 
            }
           return factura;
    }
    
    
    public Articulo borraArticulo(Connection con,Factura factura,Articulo articulo) throws Exception
    {
           Articulo articulo1=null; 
           PreparedStatement stmt=null;
           try {            
                // si hay un articulo en la factura obtenerlo antes de borrarlo
                if((articulo1=getArticuloFactura(con, factura, articulo))!=null) {
                    
                    stmt = con.prepareStatement("DELETE FROM Articulo_Factura "
                                            + " WHERE Factura_idFactura=? and Articulo_idArticulo=?");
                    stmt.setInt(1,factura.getId());
                    stmt.setInt(2,articulo.getId());
                    stmt.executeUpdate();
                }
            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al borrar el articulo en la factura "+ex.getMessage());
            }  finally
            {
                if (stmt != null) stmt.close();//Cerramos el Statement 
            }     
           return articulo1;
    }


    public void addArticulo(Connection con,Factura factura,Articulo articulo,int numero) throws Exception
    {
           PreparedStatement stmt=null;
           try {            
                stmt = con.prepareStatement("INSERT INTO Articulo_Factura VALUES(?,?,?)");
                stmt.setInt(1,articulo.getId());
                stmt.setInt(2,factura.getId());
                stmt.setInt(3,numero);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al insertar el articulo en la factura "+ex.getMessage());
            }  finally
            {
                if (stmt != null) stmt.close();//Cerramos el Statement 
            }     
    }

    public Factura getArticulosFactura(Connection con,Factura factura) throws Exception
    {
        ResultSet rs=null;
        PreparedStatement stmt=null;   
        try {
                stmt = con.prepareStatement("SELECT Articulo_idArticulo, Numero, Descripcion " +
                        "FROM  Articulo_Factura af,Articulo a " +
                        "WHERE af.Articulo_idArticulo=a.idArticulo AND af.Factura_idFactura=?");
                stmt.setInt(1,factura.getId());
                rs =stmt.executeQuery();

                Articulo articulo1=null;
                List lista = new ArrayList();
                while (rs.next()) {
                   articulo1=new Articulo();
                   articulo1.setId(rs.getInt("Articulo_idArticulo"));
                   articulo1.setDescripcion(rs.getString("Descripcion"));                    
                   articulo1.setCantidadComprada(rs.getInt("Numero"));
                   lista.add(articulo1);
                }
                factura.setArticulos(lista);
                
            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al leer los articulos de la factura "+ex.getMessage());
            } finally
            {
                if (rs != null) rs.close(); //Cerramos el resulset
                if (stmt != null) stmt.close();//Cerramos el Statement 
            }
        return factura;
    }
    public Articulo getArticuloFactura(Connection con,Factura factura,Articulo articulo) throws Exception
    {
        Articulo articulo1=null;
        ResultSet rs=null;
        PreparedStatement stmt=null;   
        try {
                stmt = con.prepareStatement("SELECT Articulo_idArticulo, Numero, Descripcion " +
                        "FROM  Articulo_Factura af, Articulo a " +
                        "WHERE af.Articulo_idArticulo=a.idArticulo "
                        + "AND af.Factura_idFactura=? "
                        + "AND af.Articulo_idArticulo=?"
                        );
                // colocamos los parametros
                stmt.setInt(1,factura.getId());
                stmt.setInt(2,articulo.getId());
                rs =stmt.executeQuery();
                // si hay algun articulo lo rellenamos
                if (rs.next()) {
                   articulo1=new Articulo();
                   articulo1.setId(rs.getInt("Articulo_idArticulo"));
                   articulo1.setDescripcion(rs.getString("Descripcion"));                    
                   articulo1.setCantidadComprada(rs.getInt("Numero"));
                }
            } catch (SQLException ex) {
                //ex.printStackTrace();
                Log.getInstance().error(ex);
                throw new ProgException("Ha habido un problema al buscar un articulo de la factura "+ex.getMessage());
            } finally
            {
                if (rs != null) rs.close(); //Cerramos el resulset
                if (stmt != null) stmt.close();//Cerramos el Statement 
            }
        return articulo1;
    }
}
