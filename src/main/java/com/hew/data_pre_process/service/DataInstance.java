package com.hew.data_pre_process.service;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Matrices;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import scala.collection.Iterator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * @author hw
 * @date 2018/5/9 13:00
 */
@Service
public class DataInstance {
    private String fileName;
    private int numNaN = 0;

    public DataInstance() {
    }

    public void getFileName(String fileName) {
        this.fileName = fileName;
    }

    public Instances getInstance() {
        String pathName = "D:/weka/Arff/" + this.fileName;
        File file = new File(pathName);
        ArffLoader loader = new ArffLoader();
        Instances instances = null;

        try {
            loader.setSource(file);
            instances = loader.getDataSet();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return instances;
    }

    public Map getRelation() {
        Map<String, Object> map = new HashMap();
        Instances instances = this.getInstance();
        String relationName = instances.relationName();
        int numAttributes = instances.numAttributes();
        int numInstance = instances.size();
        Double sumOfWeights = instances.sumOfWeights();
        map.put("relationName", relationName);
        map.put("numAttributes", numAttributes);
        map.put("numInstance", numInstance);
        map.put("sumOfWeights", sumOfWeights);
        return map;
    }

    public List getAttributes() {
        List list = new ArrayList();
        Instances instances = this.getInstance();
        int numAttributes = instances.numAttributes();

        for(int i = 0; i < numAttributes; ++i) {
            list.add(instances.attribute(i).name());
        }

        return list;
    }

    public Map getSelectedAttrbuteVar(int varIndex) {
        Map<String, Object> map = new HashMap();
        Map selectedAttrbute = this.getSelectedAttrbute();
        List listSelected = (List)selectedAttrbute.get("Name");
        String stringName = (String)listSelected.get(varIndex);
        map.put("name", stringName);
        boolean digit = this.isDigit(varIndex);
        if (!digit) {
            map.put("type", "Nominal");
        } else {
            map.put("type", "Numeric");
        }

        List listMissinggValue = (List)selectedAttrbute.get("MissingValue");
        Object missing = listMissinggValue.get(varIndex);
        map.put("missingValue", missing);
        List listMissing = (List)selectedAttrbute.get("Missing");
        Object missPer = listMissing.get(varIndex);
        map.put("missing", missPer);
        List listDistinct = (List)selectedAttrbute.get("Distinct");
        Object distinct = listDistinct.get(varIndex);
        map.put("distinct", distinct);
        List listUnique = (List)selectedAttrbute.get("Unique");
        Object unique = listUnique.get(varIndex);
        map.put("unique", unique);
        List listUniPercent = (List)selectedAttrbute.get("UniPercent");
        Object uniPercent = listUniPercent.get(varIndex);
        map.put("uniPercent", uniPercent);
        return map;
    }

    public boolean isDigit(int varIndex) {
        Instances instances = this.getInstance();
        Instance ins = instances.get(0);
        String s = ins.toString(varIndex);
        boolean b = Character.isDigit(s.charAt(0));
        boolean isDigit;
        if (b) {
            isDigit = true;
        } else {
            isDigit = false;
        }

        return isDigit;
    }

    public Map getStatisticVar(int varIndex) {
        boolean isDigit = this.isDigit(varIndex);
        Map<String, Object> map = new HashMap();
        if (isDigit) {
            Map statistic = this.getStatistic();
            List listSta = new ArrayList();
            listSta.add("Mininum");
            listSta.add("Maximum");
            listSta.add("Mean");
            listSta.add("StdDev");
            map.put("listSta", listSta);
            List listMin = (List)statistic.get("listMin");
            Object min = listMin.get(varIndex);
            map.put("min", min);
            List listMax = (List)statistic.get("listMax");
            Object max = listMax.get(varIndex);
            map.put("max", max);
            List listMean = (List)statistic.get("listMean");
            Object mean = listMean.get(varIndex);
            map.put("mean", mean);
            List listStdDev = (List)statistic.get("listStdDev");
            Object stdDev = listStdDev.get(varIndex);
            map.put("stdDev", stdDev);
            map.put("isDigit", isDigit);
            return map;
        } else {
            map.put("isDigit", isDigit);
            return map;
        }
    }

    public Map getSelectedAttrbute() {
        Map<String, Object> map = new HashMap();
        Instances instances = this.getInstance();
        int numAttributes = instances.numAttributes();
        int numInstances = instances.numInstances();
        List listAttributes = this.getAttributes();
        map.put("Name", listAttributes);
        List listDistinct = new ArrayList();
        List listInstance = new ArrayList();
        Instance ins = instances.get(0);
        String s = ins.toString(0);

        int lenth;
        for(lenth = 0; lenth < s.length(); ++lenth) {
            boolean b = Character.isDigit(s.charAt(lenth));
            if (!b) {
                map.put("type", "Nominal");
            } else {
                map.put("type", "Numeric");
            }
        }

        int j;
        for(lenth = 0; lenth < numAttributes; ++lenth) {
            j = instances.numDistinctValues(instances.attribute(lenth));
            listDistinct.add(j);
        }

        map.put("Distinct", listDistinct);

        for(lenth = 0; lenth < numAttributes; ++lenth) {
            for(j = 0; j < numInstances; ++j) {
                listInstance.add(instances.get(j).value(lenth));
            }
        }

        lenth = numInstances * numAttributes;
        double[] values = new double[lenth];

        for(int m = 0; m < lenth; ++m) {
            values[m] = (Double)listInstance.get(m);
        }

        Matrix dense = Matrices.dense(numInstances, numAttributes, values);
        Iterator<Vector> iterator = dense.colIter();
        List listMissing = new ArrayList();
        List listMissPercent = new ArrayList();
        List listUnique = new ArrayList();

        ArrayList listUniPercent;
        for(listUniPercent = new ArrayList(); iterator.hasNext(); this.numNaN = 0) {
            Vector next = (Vector)iterator.next();
            int unique = this.getUniqueVector(next);
            int missingValue = this.numNaN;
            int missing = missingValue * 100 / numInstances;
            int uniPercent = unique * 100 / numInstances;
            listMissing.add(missingValue);
            listMissPercent.add(missing);
            listUnique.add(unique);
            listUniPercent.add(uniPercent);
        }

        boolean isDigit = this.isDigit(0);
        map.put("MissingValue", listMissing);
        map.put("Missing", listMissPercent);
        map.put("Unique", listUnique);
        map.put("UniPercent", listUniPercent);
        map.put("isDigit", isDigit);
        return map;
    }

    public Map getStatistic() {
        Map<String, Object> map = new HashMap();
        Instances instances = this.getInstance();
        int numAttributes = instances.numAttributes();
        int numInstances = instances.numInstances();
        List list = new ArrayList();

        int lenth;
        for(lenth = 0; lenth < numInstances; ++lenth) {
            for(int j = 0; j < numAttributes; ++j) {
                list.add(instances.get(lenth).value(j));
            }
        }

        lenth = numInstances * numAttributes;
        double[] values = new double[lenth];

        for(int m = 0; m < lenth; ++m) {
            values[m] = (Double)list.get(m);
        }

        Matrix dense = Matrices.dense(numAttributes, numInstances, values);
        Iterator<Vector> iterator = dense.colIter();
        ArrayList vectors = new ArrayList();

        while(iterator.hasNext()) {
            Vector next = (Vector)iterator.next();
            vectors.add(next);
        }

        JavaSparkContext jsc = this.getSparkContext();
        JavaRDD<Vector> javaRDD = jsc.parallelize(vectors);
        MultivariateStatisticalSummary summary = Statistics.colStats(javaRDD.rdd());
        List listMin = new ArrayList();
        List listMax = new ArrayList();
        List listMean = new ArrayList();
        List listStdDev = new ArrayList();
        Vector min = summary.min();
        Vector max = summary.max();
        Vector mean = summary.mean();
        Vector variance = summary.variance();
        int mixSize = min.size();

        int maxSize;
        for(maxSize = 0; maxSize < mixSize; ++maxSize) {
            listMin.add(min.apply(maxSize));
        }

        maxSize = max.size();

        for(int i = 0; i < maxSize; ++i) {
            listMax.add(max.apply(i));
        }

        DecimalFormat df = new DecimalFormat("######0.000");
        int meanSize = mean.size();

        int variSize;
        for(variSize = 0; variSize < meanSize; ++variSize) {
            double meanValue = mean.apply(variSize);
            listMean.add(df.format(meanValue));
        }

        variSize = variance.size();

        for(int i = 0; i < variSize; ++i) {
            double varValue = variance.apply(i);
            listStdDev.add(df.format(Math.sqrt(varValue)));
        }

        List listSta = new ArrayList();
        listSta.add("Mininum");
        listSta.add("Maximum");
        listSta.add("Mean");
        listSta.add("StdDev");
        map.put("listSta", listSta);
        map.put("listMin", listMin);
        map.put("listMax", listMax);
        map.put("listMean", listMean);
        map.put("listStdDev", listStdDev);
        jsc.close();
        return map;
    }

    public Map getVarData(int varIndex) {
        Instances instances = this.getInstance();
        JavaSparkContext jsc = this.getSparkContext();
        Map map = new HashMap();
        List listName = new ArrayList();
        List listValue = new ArrayList();
        ArrayList listCount = new ArrayList();

        try {
            int numInstances = instances.numInstances();
            int numAttributes = instances.numAttributes();
            Instance instance1 = instances.get(0);

            for(int k = 0; k < numAttributes; ++k) {
                String name = instance1.attribute(k).name();
                listName.add(name);
            }

            List<String> list = new ArrayList();

            for(int m = 0; m < numInstances; ++m) {
                Instance instance = instances.get(m);
                String s = instance.toString(varIndex);
                list.add(s);
            }

            JavaRDD<String> word = jsc.parallelize(list);
            JavaPairRDD<String, Integer> word2int = word.mapToPair((s->new Tuple2<String ,Integer>(s,1)));
            JavaPairRDD<String, Integer> res = word2int.reduceByKey((a, b) -> a + b);
            JavaPairRDD<String, Integer> sort = res.sortByKey();
            List<Tuple2<String, Integer>> collect = sort.collect();
            java.util.Iterator var = collect.iterator();

            while(var.hasNext()) {
                Tuple2<String, Integer> t = (Tuple2)var.next();
                listValue.add(t._1);
                listCount.add(t._2);
            }

            map.put("attrName", listName.get(varIndex));
            map.put("tuple_1", listValue);
            map.put("tuple_2", listCount);
        } catch (Exception var23) {
            var23.printStackTrace();
        } finally {
            jsc.close();
        }

        return map;
    }

    public int getUniqueVector(Vector vec) {
        double[] arr = vec.toArray();
        int len = 0;
        double temp = 0.0D;

        int m;
        for(m = 0; m < arr.length; ++m) {
            temp = arr[m];

            for(int j = m + 1; j < arr.length; ++j) {
                if (temp == arr[j]) {
                    arr[m] = -1.0D;
                    arr[j] = -1.0D;
                }
            }
        }

        for(m = 0; m < arr.length; ++m) {
            if (arr[m] != -1.0D && !Double.isNaN(arr[m])) {
                ++len;
            } else if (Double.isNaN(arr[m])) {
                ++this.numNaN;
            }
        }

        return len;
    }

    public JavaSparkContext getSparkContext() {
        SparkConf sparkConf = (new SparkConf()).setAppName("nihao").set("spark.driver.allowMultipleContexts", "true").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        return jsc;
    }
}
