package emn.southcoder.e_jeep.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class EjeepTransactions {
    @SerializedName("EjeepTransactions")
    ArrayList<EjeepTransaction> ejeepTransactionArrayList;

    public EjeepTransactions(ArrayList<EjeepTransaction> ejeepTransactionArrayList) {
        this.ejeepTransactionArrayList = ejeepTransactionArrayList;
    }

    public ArrayList<EjeepTransaction> getEjeepTransactionArrayList() {
        return ejeepTransactionArrayList;
    }

    public void setEjeepTransactionArrayList(ArrayList<EjeepTransaction> ejeepTransactionArrayList) {
        this.ejeepTransactionArrayList = ejeepTransactionArrayList;
    }
}
