package me.robin.datasource;

import me.robin.utils.DesUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by Lubin.Xuan on 2015/4/6.
 * ie.
 */
public class CryptBasicDataSource extends BasicDataSource {

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public void setConnectAuthInfo(String encryptInfo) throws Exception {
        String[] sp = encryptInfo.split("@");
        DesUtils desUtils = new DesUtils(sp[1]);
        String unEncode = desUtils.decrypt(sp[0]);
        sp = unEncode.split("\t");
        String jdbcUrl = sp[2];
        String authUser = sp[0];
        String password = sp[1];
        super.setUrl(jdbcUrl);
        super.setUsername(authUser);
        super.setPassword(password);
    }
}
