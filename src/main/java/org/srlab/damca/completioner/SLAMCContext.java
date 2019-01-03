package org.srlab.damca.completioner;

import java.util.ArrayList;
import java.util.List;

public class SLAMCContext
{
    String methodCallExp;
    List<String> parameters;
    String context;

    public SLAMCContext(String methodCallExp) {
        this.methodCallExp = methodCallExp;
        this.parameters = new ArrayList<String>();
    }

    public SLAMCContext() {
        this.parameters = new ArrayList<String>();
    }

    public String getMethodCallExp() {
        return methodCallExp;
    }

    public void setMethodCallExp(String methodCallExp) {
        this.methodCallExp = methodCallExp;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void print()
    {
        System.out.println(this.methodCallExp+" "+this.getParameters()+" "+this.getContext());
    }
}
