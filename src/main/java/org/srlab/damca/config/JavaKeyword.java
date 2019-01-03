package org.srlab.damca.config;

import org.srlab.damca.completioner.SLAMCContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaKeyword
{
    private static Map<String,String> operator;
    private static Map<String,String> openclose;
    private static Map<String,String> keyword;
    private static Map<String,String> lineKeyword;
    public static Map<String,String> javaOperator()
    {
        operator = new HashMap<String, String>();
        operator.put("=","OP[assign]");
        operator.put("+","OP[sum]");
        operator.put("-","OP[sub]");
        operator.put("*","OP[multiply]");
        operator.put("/","OP[divide]");
        operator.put("%","OP[remainder]");
        operator.put("==","OP[isEqual]");
        operator.put("!=","OP[notEqual]");
        operator.put("!","OP[not]");
        operator.put("<=","OP[lessOrEqual]");
        operator.put(">=","OP[greaterOrEqual]");
        operator.put("<","OP[lessThan]");
        operator.put(">","OP[greaterThan]");
        operator.put("<","OP[lessThan]");
        operator.put("+=","OP[shortHandSum]");
        operator.put("-=","OP[shortHandSub]");
        operator.put("*=","OP[shortHandMultiply]");
        operator.put("/=","OP[shortHandDivide]");
        operator.put("%=","OP[shortHandRemainder]");
        operator.put("++","OP[increment]");
        operator.put("--","OP[decrement]");
        operator.put("?","OP[ternaryTrue]");
        operator.put(":","OP[ternaryFalse]");
        operator.put("||","OP[logicalOr]");
        operator.put("&&","OP[logicalAnd]");
        operator.put("|","OP[bitwiseOr]");
        operator.put("&","OP[bitwiseAnd]");
        operator.put(">>","OP[shiftRight]");
        operator.put("<<","OP[shiftLeft]");
        operator.put(".","OP[access]");
        return operator;
    }
    public static Map<String,String> javaOpenClose()
    {
        openclose = new HashMap<String, String>();
        openclose.put("(","PARENTHESESSTART");
        openclose.put(")","PARENTHESESEND");
        openclose.put("{","CURLYSTART");
        openclose.put("}","CURLYEND");
        openclose.put("[","ARRAYSTART");
        openclose.put("]","ARRAYEND");
        openclose.put("null","NULL");
        openclose.put(";","EOL");
        openclose.put(",","COMMA");
        return openclose;
    }

    public static Map<String,String> javaKeyword()
    {
        keyword = new HashMap<String, String>();
        String keywords[]={"String","abstract",	"continue",	"goto",	"package",	"switch",
                "assert",	"default",	"if",	"this",
                "boolean",	"do","implements"	,"throw",
                "break",	"double",	"import",	"throws",
                "byte",	"else",	"instanceof",	"return",	"transient",
                "case",	"extends",	"int",	"short",	"try",
                "catch",	"final",	"interface",	"static",	"void",
                "char",	"finally",	"long",	"strictfp",	"volatile",
                "class",	"native",	"super",	"while",
                "const",	"for",	"new",	"synchronized"};

        for(String k:keywords){
            keyword.put(k, k);
        }
        return keyword;
    }

    public static Map<String,String> javaLineKeyword()
    {
        lineKeyword = new HashMap<String, String>();
        String lineKeywords[]={"String","abstract",	"continue",	"goto",	"package",	"switch",
                "assert",	"default",	"if",	"this",
                "boolean",	"do","implements"	,"throw",
                "break",	"double",	"import",	"throws",
                "byte",	"else",	"instanceof",	"return",	"transient",
                "case",	"extends",	"int",	"short",	"try",
                "catch",	"final",	"interface",	"static",	"void",
                "char",	"finally",	"long",	"strictfp",	"volatile",
                "class",	"native",	"super",	"while",
                "const",	"for",	"new",	"synchronized","[","]","="};

        for(String k:lineKeywords){
            lineKeyword.put(k, k);
        }
        return lineKeyword;
    }


    public static List<String> getContext(Map<String,String> SLAMCToken,String token)
    {
        List<String> context = new ArrayList<String>();
        if(SLAMCToken.containsKey(token))
        {
            context.add(SLAMCToken.get(token));
        }
        else if(keyword.containsKey(token))
        {
            context.add(keyword.get(token).toUpperCase());
        }
        else if(operator.containsKey(token))
        {
            context.add(operator.get(token));
        }
        else if(openclose.containsKey(token))
        {
            context.add(openclose.get(token));
        }
        else
        {
            //System.out.println(token);
            context.add("LEX["+token+"]");
        }

        return context;
    }
}
