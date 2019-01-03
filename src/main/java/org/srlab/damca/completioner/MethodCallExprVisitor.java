package org.srlab.damca.completioner;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.google.common.collect.Lists;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;
import org.srlab.damca.config.Config;
import org.srlab.damca.config.JavaKeyword;
import org.srlab.damca.node.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class MethodCallExprVisitor extends VoidVisitorAdapter<Void> {

    private List<ModelEntry> modelEntryList;
    private List<List<String>> SLPContext;
    private List<SLAMCContext> SLAMCContexts;
    private String filePath;
    private CompilationUnit cu;
    private HashMap<String, String> hmKeyword;
    private HashMap<String, String> hmLineKeyword;
    private HashMap<String, String> hmOperator;
    private HashMap<String, String> hmOpenClose;
    public static int NL = 4;
    private BufferedWriter logbw;
    private String subjectSystem;
    private long counter;
    private static Map<String, String> SLAMCToken;

    //COnstructor
    public MethodCallExprVisitor(CompilationUnit _cu,List<ModelEntry> modelEntryList,List<List<String>> SLPContext,List<SLAMCContext> SLAMCContexts, String _path, BufferedWriter logbw, String subjectSystem) {
        // TODO Auto-generated constructor stub
        this.cu = _cu;
        this.modelEntryList = modelEntryList;
        this.SLPContext = SLPContext;
        this.SLAMCContexts = SLAMCContexts;
        this.filePath = _path;
        this.hmKeyword = new HashMap<>(JavaKeyword.javaKeyword());
        this.hmLineKeyword = new HashMap<>(JavaKeyword.javaLineKeyword());
        this.hmOperator = new HashMap<String, String>(JavaKeyword.javaOperator());
        this.hmOpenClose = new HashMap<>(JavaKeyword.javaOpenClose());
        this.logbw = logbw;
        this.subjectSystem = subjectSystem;
        counter = 1;
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MethodDeclaration getMethodDeclarationContainer(Node node) {
        Optional<Node> parent = node.getParentNode();
        while (parent.isPresent() && ((parent.get() instanceof MethodDeclaration)) == false) {
            parent = parent.get().getParentNode();
        }
        if (parent.isPresent() && ((parent.get()) instanceof MethodDeclaration)) {
            return (MethodDeclaration) parent.get();
        } else return null;
    }

    public String getLineContent(List<Token> stList, int index) {
        List<String> list = new ArrayList();
        int newLineNumber = stList.get(index).getLine();
        int oldLineNumber = stList.get(index).getLine();
        for (int j = index, k = 0; j >= 0 && stList.get(j).getLine() >= 1 && k < 1; j--) {
            Pattern patt = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)\\.([a-zA-Z0-9\\_]+)");
            Pattern patt2 = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)");

            Matcher m = patt.matcher(stList.get(j).getToken());
            Matcher m2 = patt2.matcher(stList.get(j).getToken());

            if (m.matches()) {
                if ((j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {
                    String s = m.group(4);//constants are usually written in the upper case letter

                    if (m.group(4).toUpperCase().equals(s)) {
                    } else {

                        if (Character.isUpperCase(m.group(4).charAt(0)))
                            list.add(m.group(4));
                        else {
                            list.add(m.group(4));
                        }
                    }
                }
            } else if (m2.matches() && (j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {
                if ((j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {

                    String s = m2.group(3);//constants are usually written in the upper case letter
                    if (m2.group(3).toUpperCase().equals(s)) {

                    } else {
                        if (Character.isUpperCase(m2.group(3).charAt(0)))
                            list.add(m2.group(3));
                        else {
                            list.add(m2.group(3));
                        }
                    }
                }
            } else {
                if (this.hmLineKeyword.containsKey(stList.get(j).getToken())) {
                    list.add(stList.get(j).getToken());
                } else if (stList.get(j).getToken().equals("(") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals(",") || stList.get(j).getToken().equals("{") || stList.get(j).getToken().equals("(") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals("}") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals(",") == true || stList.get(j).getToken().equals(";") == true || Character.isUpperCase(stList.get(j).getToken().charAt(0)) == false) {

                } else
                    list.add(stList.get(j).getToken());
            }
            newLineNumber = stList.get(j).getLine();
            if (oldLineNumber != newLineNumber) {
                k++;
                oldLineNumber = newLineNumber;
            }
        }

        StringBuffer sb = new StringBuffer("");
        if (list.size() > 0) {
            String old = list.get(0);
            sb.append(old);
            sb.append(" ");
            for (int i = 1; i < list.size(); i++) {
                String str_new = list.get(i);
                if (old.equals(str_new) == false) {
                    sb.append(list.get(i));
                    old = str_new;
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }

    public String calcNeighbor(List<Token> stList, int index) throws FileNotFoundException {
        List<String> list = new ArrayList();
        int newLineNumber = stList.get(index).getLine();
        int oldLineNumber = stList.get(index).getLine();
        int startLineNumber = oldLineNumber;
        for (int j = index, k = 0; j >= 0 && stList.get(j).getLine() >= 1 && k < MethodCallExprVisitor.NL; j--) {

            Pattern patt = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)\\.([a-zA-Z0-9\\_]+)");
            Pattern patt2 = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)");


            Matcher m = patt.matcher(stList.get(j).getToken());
            Matcher m2 = patt2.matcher(stList.get(j).getToken());


            if (m.matches()) {
                if ((j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {

                    String s = m.group(4);//constants are usually written in the upper case letter

                    if (m.group(4).toUpperCase().equals(s)) {
                    } else {

                        if (Character.isUpperCase(m.group(4).charAt(0))) {
                            list.add(m.group(4));
                        } else {
                            list.add(m.group(4));
                        }
                    }
                }
            } else if (m2.matches() && (j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {
                if ((j + 1) < stList.size() && stList.get(j + 1).getToken().startsWith("(")) {
                    String s = m2.group(3);//constants are usually written in the upper case letter

                    if (m2.group(3).toUpperCase().equals(s)) {
                        //list.add(m.group(3));
                    } else {
                        if (Character.isUpperCase(m2.group(3).charAt(0))) {
                            list.add(m2.group(3));
                        } else {
                            list.add(m2.group(3));
                        }
                    }
                }
            } else {
                if (this.hmKeyword.containsKey(stList.get(j).getToken())) {
                    list.add(stList.get(j).getToken());
                } else if (stList.get(j).getToken().equals("(") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals(",") || stList.get(j).getToken().equals("{") || stList.get(j).getToken().equals("(") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals("}") || stList.get(j).getToken().equals(")") || stList.get(j).getToken().equals(",") == true || stList.get(j).getToken().equals(";") == true || Character.isUpperCase(stList.get(j).getToken().charAt(0)) == false) {
                }
            }
            newLineNumber = stList.get(j).getLine();
            if (oldLineNumber != newLineNumber) {
                k++;
                oldLineNumber = newLineNumber;
            }
        }

        StringBuffer sb = new StringBuffer("");
        if (list.size() > 0) {
            String old = list.get(0);
            sb.append(old);
            sb.append(" ");
            for (int i = 1; i < list.size(); i++) {
                String str_new = list.get(i);
                if (old.equals(str_new) == false) {
                    sb.append(list.get(i));
                    old = str_new;
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }


    public String getPreviousMethod(List<Token> tokenList, String recieverVariable) throws IOException {
        String returnValue = "";
        for (Token token : tokenList) {
            if (token.getToken().contains(recieverVariable)) {
                String name = token.getToken();
                name = name.substring(name.indexOf(".") + 1);
                returnValue = returnValue + " " + name;
            }
        }
        return returnValue;
    }


    public List<Token> tokenize(String input) throws IOException {

        //Step-1: declare return object
        List<Token> tokenList = new ArrayList();


        //Step-2: initialize StreamTokenizer to tokenize source code
        InputStream is = new ByteArrayInputStream(input.getBytes());
        StreamTokenizer streamTokenizer = new StreamTokenizer(new InputStreamReader(is));
        streamTokenizer.parseNumbers();
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.eolIsSignificant(false);
        streamTokenizer.slashSlashComments(true);
        streamTokenizer.slashStarComments(true);


        //Step-3: collect list of tokens
        int tokenNumber = 0;
        int token = streamTokenizer.nextToken();
        while (token != StreamTokenizer.TT_EOF) {

            if (streamTokenizer.sval != null && streamTokenizer.sval.matches("\\s+")) {
                tokenNumber++;
            }

            switch (token) {

                case StreamTokenizer.TT_NUMBER:
                    double num = streamTokenizer.nval;
                    break;
                case StreamTokenizer.TT_WORD:
                    tokenList.add(new Token(streamTokenizer.sval, streamTokenizer.lineno()));
                    break;
                case '"':
                    String dquoteVal = streamTokenizer.sval;
                    break;
                case '\'':
                    String squoteVal = streamTokenizer.sval;
                    break;
                case StreamTokenizer.TT_EOL:
                    break;
                case StreamTokenizer.TT_EOF:
                    break;

                default:
                    char ch = (char) streamTokenizer.ttype;
                    tokenList.add(new Token("" + ch, streamTokenizer.lineno()));
                    break;
            }
            token = streamTokenizer.nextToken();
        }
        is.close();
        return tokenList;
    }

    //input points to complete source code. Start is the start and end is of Position type
    private String collectSourceString(String input, Position start, Position end) throws IOException {
        int idxStart = -1;
        int curIdx = 0;
        int curLine = 1;

        while (curLine <= end.line) {
            if (curLine >= start.line && curLine <= end.line) {
                if (curLine == start.line && idxStart == -1) {
                    idxStart = curIdx + start.column - 1;
                }
                if (curLine == end.line) {
                    for (int column = 0; column < end.column; column++) {
                        curIdx++;
                    }
                    curLine++;
                }
            }
            if (input.charAt(curIdx) == '\n')
                curLine++;

            curIdx++;
        }
        logbw.write("Start = " + idxStart + " End = " + curIdx);
        logbw.newLine();
        return input.substring(idxStart, curIdx);
    }

    private SLAMCContext SLAMCContextCollection(List<Token> tokenList, MethodCallExpr methodCallExpr) {
        SLAMCContext slamcContext = new SLAMCContext();

        //Collecting Method Call Expression for SLAMC
        int parameter_count = methodCallExpr.getArguments().size();
        String name = methodCallExpr.getName().asString();
        Optional<Expression> scope = methodCallExpr.getScope();
        TypeDescriptor typeDescriptor = Resolver.resolveScope(scope.get());
        String returnType = Resolver.resolveReturnType(methodCallExpr);
        if (typeDescriptor != null) {
            if (returnType != null)
                slamcContext.setMethodCallExp("CALL["+ typeDescriptor.getTypeQualifiedName() +","+ name +","+ parameter_count +","+ returnType +"]");
                //System.out.println( "CALL[" + typeDescriptor.getTypeQualifiedName() + ", " + name + ", " + parameter_count +", " +returnType+"]");
            else
                slamcContext.setMethodCallExp("CALL["+ typeDescriptor.getTypeQualifiedName()+ ","+ name +","+ parameter_count +"]");
            //System.out.println("CALL[" + typeDescriptor.getTypeQualifiedName() + ", " + name + ", " + parameter_count +"]");
        }

        //Collecting Context for SLAMC
        List<String> context = new ArrayList<String>();
        if (tokenList.size() - 1 >= 4) {
            for (int z = tokenList.size() - 2; z >= tokenList.size() - 5; z--) {
                String token = tokenList.get(z).getToken();
                context.addAll(JavaKeyword.getContext(SLAMCToken, token));

            }
        } else {
            for (int z = tokenList.size() - 2; z >= tokenList.size(); z--) {
                String token = tokenList.get(z).getToken();
                context.addAll(JavaKeyword.getContext(SLAMCToken, token));
            }

        }
        Collections.reverse(context);
        String finalContext = "";
        for (String each_context : context)
            finalContext = finalContext + " "+each_context;

        slamcContext.setContext(finalContext);

        return slamcContext;
    }

    private static void semanticExtraction(Node node) {
        if (node instanceof SimpleName) {
            TypeDescriptor typeDescriptor = Resolver.resolveSimpleName(node);

            if (typeDescriptor == null) {
                if (Character.isUpperCase(node.toString().charAt(0)))
                    SLAMCToken.put(node.toString(), "TYPE["+ node +"]");
                //System.out.println(node + " TYPE[" + node + "]");
            } else
                SLAMCToken.put(node.toString(), "VAR["+ typeDescriptor.getTypeQualifiedName() +"]");
            //System.out.println(node+"VAR["+typeDescriptor.getTypeQualifiedName()+"]");

        } else if (node instanceof StringLiteralExpr) {
            SLAMCToken.put(node.toString(), "LIT[String]");
            //System.out.println("LIT[String]");
        } else if (node instanceof DoubleLiteralExpr) {
            SLAMCToken.put(node.toString(), "LIT[Double]");
        } else if (node instanceof LongLiteralExpr) {
            SLAMCToken.put(node.toString(), "LIT[Long]");
        } else if (node instanceof IntegerLiteralExpr) {
            SLAMCToken.put(node.toString(), "LIT[Integer]");
        } else if (node instanceof CharLiteralExpr) {
            SLAMCToken.put(node.toString(), "LIT[Character]");
        } else if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) node;
            String name = methodDeclaration.getNameAsString();
            String returnType = methodDeclaration.getTypeAsString();
            String dec = methodDeclaration.getDeclarationAsString();
            String parameter = dec.substring(dec.indexOf('(') + 1, dec.indexOf(')'));
            String[] tokens = parameter.split(",");
            List<String> parameters = new ArrayList<String>();
            for (String token : tokens) {
                String[] temp = token.split(" ");
                parameters.add(temp[0]);
            }
            SLAMCToken.put(node.toString(), "FUNC["+ name +",PARA"+ parameters +","+ returnType +"]");
            //System.out.println("FUNC["+name+", PARA"+parameters+", "+returnType+"]");
        } else if (node instanceof MethodCallExpr) {
            MethodCallExpr methodCallExpr = (MethodCallExpr) node;
            int parameter_count = methodCallExpr.getArguments().size();
            String name = methodCallExpr.getName().asString();
            Optional<Expression> scope = methodCallExpr.getScope();
            TypeDescriptor typeDescriptor = Resolver.resolveScope(scope.get());
            String returnType = Resolver.resolveReturnType(methodCallExpr);
            if (typeDescriptor != null) {
                if (returnType != null)
                    SLAMCToken.put(node.toString(), "CALL["+ typeDescriptor.getTypeQualifiedName() +","+ name +","+ parameter_count +"," + returnType +"]");
                else
                    SLAMCToken.put(node.toString(), "CALL["+ typeDescriptor.getTypeQualifiedName() +","+ name +","+ parameter_count +"]");
            }
            //System.out.println("CALL["+typeDescriptor.getTypeQualifiedName()+", "+name+", "+parameter_count+"]");
        } else if (node instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
            SimpleName simpleName = fieldAccessExpr.getName();
            Expression expression = fieldAccessExpr.getScope();
            TypeDescriptor typeDescriptor = Resolver.resolveScope(expression);
            if (typeDescriptor != null)
                SLAMCToken.put(node.toString(), "FIELD["+ simpleName +","+ typeDescriptor.getTypeQualifiedName() +"]");
            //  System.out.println(node+" FIELD["+simpleName+", "+typeDescriptor.getTypeQualifiedName()+"]");

        } else if (node instanceof CastExpr) {
            CastExpr castExpr = (CastExpr) node;
            SLAMCToken.put(node.toString(), "CAST["+ castExpr.getType() +"]");
            //System.out.println(node+" "+ castExpr.getType()+" "+ castExpr.getExpression());
        }
    }

    @Override
    public void visit(MethodCallExpr m, Void arg) {
        super.visit(m, arg);
        if (m.getScope().isPresent()) {
            try {
                //Reolve the type binding for method decleration
                SymbolReference<ResolvedMethodDeclaration> srResolvedMethodDeclaration = JSSConfigurator.getInstance(logbw).getJpf().solve(m);

                if (srResolvedMethodDeclaration.isSolved() && srResolvedMethodDeclaration.getCorrespondingDeclaration() != null) {

                    ResolvedMethodDeclaration resolvedMethodDeclaration = srResolvedMethodDeclaration.getCorrespondingDeclaration();

                    //If the packages are same as described in config.java class
                    if (Config.isInteresting(resolvedMethodDeclaration.getQualifiedName())) {
                        MethodDeclaration methodDeclaration = this.getMethodDeclarationContainer(m);
                        if (methodDeclaration != null && methodDeclaration.getBegin().isPresent() && m.getBegin().isPresent()) {
                            //System.out.println("Method Call Expr: "+m+" File: "+this.getFilePath()+" Line: "+m.getBegin().get().line);
                            logbw.write("Method Call Expr: " + m + " File: " + this.getFilePath() + " Line: " + m.getBegin().get().line);
                            logbw.newLine();


                            //Collect the source code from the begaining of the method decleration to the position of method invocation
                            String source = FileUtils.readFileToString(new File(this.filePath));
                            String text = this.collectSourceString(source, methodDeclaration.getBegin().get(), m.getBegin().get());

                            List<Token> tokenList = this.tokenize(text);

                            MethodCallEntity methodCallEntity = new MethodCallEntity();
                            String prevMethodArg="",prevMethods="",neighborList="",lineContent="";
                            if(Config.IS_DAMCA_COLLECT)
                            {
                                prevMethods = this.getPreviousMethod(tokenList, m.getScope().get().toString() + ".");
                                neighborList = this.calcNeighbor(tokenList, tokenList.size() - 1);
                                lineContent = this.getLineContent(tokenList, tokenList.size() - 1);

                                methodCallEntity = MethodCallEntity.get(m, resolvedMethodDeclaration, JSSConfigurator.getInstance(logbw).getJpf(), logbw);

                            }


                            List<String> finalContext= new ArrayList<>();
                            if(Config.IS_SLP_COLLECT)
                            {
                                List<String> eachslpContext = new ArrayList<>();

                                if (tokenList.size() >= 10) {
                                    for (int z = tokenList.size() - 1; z >= tokenList.size() - 9; z--) {
                                        eachslpContext.add(tokenList.get(z).getToken());
                                        //System.out.println(tokenList.get(z).getToken());
                                    }
                                } else {
                                    for (Token s : tokenList)
                                        eachslpContext.add(s.getToken());
                                }
                                finalContext = Lists.reverse(eachslpContext);

                                finalContext.add(".");
                                finalContext.add(m.getName().getIdentifier());
                                finalContext.add("(");
                            }


                            SLAMCContext slamcContext = new SLAMCContext();
                            if(Config.IS_SLAMC_COLLECT)
                            {
                                Optional<BlockStmt> exp = methodDeclaration.getBody();
                                if (exp.isPresent()) {
                                    NodeList<Statement> stmts = exp.get().getStatements();
                                    SLAMCToken = new HashMap<String, String>();
                                    for (Node stmt : stmts) {
                                        Stream<Node> nodes = stmt.stream();
                                        nodes.forEach(MethodCallExprVisitor::semanticExtraction);
                                    }
                                    slamcContext = SLAMCContextCollection(tokenList, m);
                                }
                            }


                            List<ParameterContent> parameterContentList = new ArrayList();
                            List<String> SLAMCparamters = new ArrayList<>();

                            if (m.getArguments().size() > 0) {
                                for (int i = 0; i < m.getArguments().size(); i++) {
                                    Expression expression = m.getArguments().get(i);
                                    if(Config.IS_SLP_COLLECT)
                                        finalContext.add(expression.toString());
                                    if (expression instanceof StringLiteralExpr) {
                                        ParameterContent parameterContent = new StringLiteralContent((StringLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Stirng]]");
                                    } else if (expression instanceof NullLiteralExpr) {
                                        ParameterContent parameterContent = new NullLiteralContent((NullLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Null]]");
                                    } else if (expression instanceof BooleanLiteralExpr) {
                                        ParameterContent parameterContent = new BooleanLiteralContent((BooleanLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Boolean]]");
                                    } else if (expression instanceof DoubleLiteralExpr) {
                                        ParameterContent parameterContent = new NumberLiteralContent((DoubleLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Double]]");
                                    } else if (expression instanceof LongLiteralExpr) {
                                        ParameterContent parameterContent = new NumberLiteralContent((LongLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Long]]");
                                    } else if (expression instanceof IntegerLiteralExpr) {
                                        ParameterContent parameterContent = new NumberLiteralContent((IntegerLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Integer]]");
                                    } else if (expression instanceof CharLiteralExpr) {
                                        ParameterContent parameterContent = new CharLiteralContent((CharLiteralExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[LIT[Character]]");
                                    } else if (expression instanceof NameExpr) {
                                        ParameterContent parameterContent = new NameExprContent((NameExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                        {
                                            String s = this.getPreviousMethod(tokenList, expression.toString() + ".");
                                            if (!s.isEmpty())
                                                prevMethodArg = prevMethodArg + " " + s;
                                            parameterContentList.add(parameterContent);
                                        }
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[VAR["+ ((NameExprContent) parameterContent).getTypeQualifiedName() +"]]");
                                    } else if (expression instanceof FieldAccessExpr) {
                                        ParameterContent parameterContent = new QualifiedNameContent((FieldAccessExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[FIELD["+ ((QualifiedNameContent) parameterContent).getScopeTypeQualifiedName() +"]]");
                                    } else if (expression instanceof ObjectCreationExpr) {
                                        ParameterContent parameterContent = new ClassInstanceCreationContent((ObjectCreationExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[OBJCREATE["+ ((ClassInstanceCreationContent) parameterContent).getTypeQualifiedName() +"]]");
                                    } else if (expression instanceof CastExpr) {
                                        ParameterContent parameterContent = new CastExpressionContent((CastExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[CAST["+ parameterContent.getParameterExpressionType() +"]]");
                                    } else if (expression instanceof MethodCallExpr) {
                                        ParameterContent parameterContent = new MethodInvocationContent((MethodCallExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[CALL["+ ((MethodInvocationContent) parameterContent).getReceiverTypeQualifiedName() +","+ ((MethodInvocationContent) parameterContent).getMethodName() +"]]");
                                    } else if (expression instanceof ThisExpr) {
                                        ParameterContent parameterContent = new ThisExpressionContent((ThisExpr) expression, logbw);
                                        if(Config.IS_DAMCA_COLLECT)
                                            parameterContentList.add(parameterContent);
                                        if(Config.IS_SLAMC_COLLECT)
                                            SLAMCparamters.add("PARA[THIS["+ ((ThisExpressionContent) parameterContent).getClassQualifiedName() +"]]");
                                    } else {
                                        throw new RuntimeException("Unknown Expression Exception Type");
                                    }

                                }
                            }

                            if(Config.IS_DAMCA_COLLECT)
                            {
                                ModelEntry modelEntry = new ModelEntry(methodCallEntity, parameterContentList, neighborList, lineContent, prevMethods, prevMethodArg);
                                this.modelEntryList.add(modelEntry);

                                if (modelEntryList.size() >= 100) {
                                    counter = ModelEntryCollectionDriver.writeContext(modelEntryList, subjectSystem, counter);
                                    modelEntryList.clear();
                                }
                            }

                            if(Config.IS_SLP_COLLECT)
                            {
                                finalContext.add(")");
                                this.SLPContext.add(finalContext);
                            }

                            if(Config.IS_SLAMC_COLLECT)
                            {
                                slamcContext.setParameters(SLAMCparamters);
                                this.SLAMCContexts.add(slamcContext);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //System.out.println("Fail to resolve type");
            }
        }
    }

    public List<SLAMCContext> getSLAMCContexts() {
        return SLAMCContexts;
    }

    public List<ModelEntry> getModelEntryList() {
        return modelEntryList;
    }

    public List<List<String>> getSLPContext() {
        return this.SLPContext;
    }

    public long getCounter() {
        return counter;
    }
}



								/*System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++=");
								System.out.println("Expression expression: "+m);
								System.out.println("Method Name: "+m.getName().getIdentifier() +" Scope: "+m.getScope().get());
								System.out.println("QN: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName());
								System.out.println("Package Name: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getPackageName());
								System.out.println("Class Name: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName());
								System.out.println(": "+resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName());

								System.out.println("MethoDeclaration: Start"+methodDeclaration.getBegin().get() +"End: "+methodDeclaration.getEnd().get()+ "MethodCallExpr: "+m.getBegin().get());
								*/
