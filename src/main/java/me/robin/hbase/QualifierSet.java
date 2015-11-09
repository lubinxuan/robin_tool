package me.robin.hbase;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Lubin.Xuan on 2014/12/10.
 */
public class QualifierSet {
    private List<String> qualifierSet = new LinkedList<String>();

    private List<byte[]> qualifierByte = new LinkedList<byte[]>();

    private String family;
    private byte[] familyByte;

    private QualifierSet(String family, Set<String> fields) {
        this.family = family;
        this.familyByte = Bytes.toBytes(this.family);
        fillQualifier(fields);
    }

    private void fillQualifier(Set<String> fields) {
        if (null != fields && !fields.isEmpty()) {
            for (String field : fields) {
                if (StringUtils.isNotBlank(field) && !qualifierSet.contains(field)) {
                    qualifierSet.add(field.trim());
                    qualifierByte.add(Bytes.toBytes(field.trim()));
                }
            }
        }
    }

    public QualifierSet(Set<String> fields) {
        this(HBaseSolrData.DATA_FAMILY, fields);
    }

    public QualifierSet(SolrQuery solrQuery) {
        this(HBaseSolrData.DATA_FAMILY, null);
        if (!"*, score".equalsIgnoreCase(solrQuery.getFields())) {
            String[] fields = solrQuery.getFields().split(",");
            Set<String> stringSet = new HashSet<String>();
            for (String f : fields) {
                if (StringUtils.isNotBlank(f)) {
                    stringSet.add(f);
                }
            }
            fillQualifier(stringSet);
        }
    }

    public QualifierSet(String filedStr) {
        this(HBaseSolrData.DATA_FAMILY, null);
        if (StringUtils.isNotBlank(filedStr)) {
            String[] fields = filedStr.split(",");
            Set<String> stringSet = new HashSet<String>();
            for (String f : fields) {
                if (StringUtils.isNotBlank(f)) {
                    stringSet.add(f);
                }
            }
            fillQualifier(stringSet);
        }
    }

    public QualifierSet() {
        this(HBaseSolrData.DATA_FAMILY, null);
    }

    public Get fillQualifier(Get get) {
        for (String qualifier : qualifierSet) {
            get.addColumn(familyByte, Bytes.toBytes(qualifier));
        }
        return get;
    }

    public Scan fillQualifier(Scan scan) {
        for (String qualifier : qualifierSet) {
            scan.addColumn(familyByte, Bytes.toBytes(qualifier));
        }
        return scan;
    }

    public List<String> getQualifierSet() {
        return qualifierSet;
    }

    public String getFamily() {
        return family;
    }

    public List<byte[]> getQualifierByte() {
        return qualifierByte;
    }

    public byte[] getFamilyByte() {
        return familyByte;
    }
}
