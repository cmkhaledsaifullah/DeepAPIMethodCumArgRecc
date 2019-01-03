package org.srlab.damca.completioner;

import java.io.Serializable;

import com.github.javaparser.Position;

/**
 * A position in a source file. Lines and columns start counting at 1
 * SPosition is the serialized version of Java position class
 */
public class SourcePosition extends Position implements Serializable {
      /**
     * The first position in the file
     */
    public static final SourcePosition HOME = new SourcePosition(1, 1);

    public SourcePosition(Position position) {
    	super(position.line,position.column);
    }
    public SourcePosition(int line, int column) {
    	super(line,column);
    }

    /**
     * Convenient factory method.
     */
    public static SourcePosition pos(int line, int column) {
        return new SourcePosition(line, column);
    }

    public SourcePosition withColumn(int column) {
        return new SourcePosition(this.line, column);
    }

    public SourcePosition withLine(int line) {
        return new SourcePosition(line, this.column);
    }
}