package MMOSystem.utils;


import MMOSystem.MMOSystem;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtil {
    //驱动信息
    private static String DRIVER;
    //数据库地址
    private static String URL;
    private Connection connection;
    private HashMap<String, PreparedStatement> pstmts = new HashMap<>();
    private ResultSet resultSet;

    public DBUtil() {
        DRIVER = "org.sqlite.JDBC";
        URL = "jdbc:sqlite:"+MMOSystem.getPugin().getDataFolder() + "/mmo.db";
    }

    /**
     * 获得数据库的连接
     */
    public void makeConnection() {
        try {
            Class.forName(DRIVER);
            System.out.println("数据库驱动启动成功！");
        } catch (Exception e) {
            System.out.println("数据库驱动启动失败！");
        }
        try {
            connection = DriverManager.getConnection(URL);
            //System.out.println(URL);
            System.out.println("数据库连接成功！");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.out.println("数据库连接失败！");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            //
        }
    }


    /**
     * 增加、删除、改
     */
    public void updateByPreparedStatement(String sql, List<Object> params) throws SQLException {
        PreparedStatement pstmt = pstmts.get(sql);
        if (pstmt == null) {
            pstmt = connection.prepareStatement(sql);
            pstmts.put(sql, pstmt);
        }
        int index = 1;
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        pstmt.executeUpdate();
    }

    /**
     * 查询单条记录
     */
    public Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        int index = 1;
        PreparedStatement pstmt = pstmts.get(sql);
        if (pstmt == null) {
            pstmt = connection.prepareStatement(sql);
            pstmts.put(sql, pstmt);
        }
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();//返回查询结果
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount();
        while (resultSet.next()) {
            for (int i = 0; i < col_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
        }
        return map;
    }

    /**
     * 查询多条记录
     */
    public List<Map<String, Object>> findModeResult(String sql, List<Object> params) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        int index = 1;
        PreparedStatement pstmt = pstmts.get(sql);
        if (pstmt == null) {
            pstmt = connection.prepareStatement(sql);
            pstmts.put(sql, pstmt);
        }
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
            list.add(map);
        }

        return list;
    }

    /**
     * 通过反射机制查询单条记录
     */
    public <T> T findSimpleRefResult(String sql, List<Object> params,
                                     Class<T> cls) throws Exception {
        T resultObject = null;
        int index = 1;
        PreparedStatement pstmt = pstmts.get(sql);
        if (pstmt == null) {
            pstmt = connection.prepareStatement(sql);
            pstmts.put(sql, pstmt);
        }
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while (resultSet.next()) {
            resultObject = cls.newInstance();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                field.setAccessible(true);
                field.set(resultObject, cols_value);
            }
        }
        return resultObject;

    }

    /**
     * 通过反射机制查询多条记录
     */
    public <T> List<T> findMoreRefResult(String sql, List<Object> params,
                                         Class<T> cls) throws Exception {
        List<T> list = new ArrayList<T>();
        int index = 1;
        PreparedStatement pstmt = pstmts.get(sql);
        if (pstmt == null) {
            pstmt = connection.prepareStatement(sql);
            pstmts.put(sql, pstmt);
        }
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        while (resultSet.next()) {
            //通过反射机制创建一个实例
            T resultObject = cls.newInstance();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                Field field = cls.getDeclaredField(cols_name);
                field.setAccessible(true); //打开javabean的访问权限
                field.set(resultObject, cols_value);
            }
            list.add(resultObject);
        }
        return list;
    }

    /**
     * 释放数据库连接
     */
    public void releaseConn() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}