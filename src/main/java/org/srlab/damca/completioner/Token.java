package org.srlab.damca.completioner;

class Token{
	String token;
	int line;
	
	public Token(){
		this.token = null;
		this.line  = -1;
	}
	public Token(String _token, int _line){
		this.token = _token;
		this.line =_line;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
}