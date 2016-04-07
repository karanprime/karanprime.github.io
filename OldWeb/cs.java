import java.applet.*;
import java.awt.*;
import java.math.*;
import java.lang.*;
import java.util.Random;
import java.awt.event.*;

/*      <applet code="cs" width=1300 height=700>
         </applet>
*/
public class cs extends Applet 
implements ActionListener, Runnable, MouseListener, MouseMotionListener  {
  //initializing parameters      
  double dt,t;

  Dimension size;                  // The size of the applet
  Image buffer;                    // The off-screen image for double-buffering
  Graphics2D big;
  Graphics bufferGraphics;         // A Graphics object for the buffer
  Thread animator;                 // Thread that performs the animation
  Color background, deadCol, liveCol;
  boolean please_stop=false;
  boolean stini=false; //0 for restart, 1 for start
  boolean twoTrue = false; 
  boolean startScreen=false;
  String msg="";

  int mode=1;//1:GOL, 2:spiral
  int row;
  int col;//assume square 2d world =)
  int cellSize;
  int space=1;
  int ystart;
  int xstart;
  int speed = 100; //30ms
  int actDuration=10;
  int deactDuration=20;//duration to stay deactivated after activation
  boolean [][] now;//= new boolean [row][col];
  boolean [][] now2;  // changed 
  boolean [][] next;// = new boolean [row][col];
  boolean [][] next2; // changed 
  boolean [][] deactiveMatrix;//to temporary store deactivated cells for initialization
  int [][][] mode2Properties;
  //0: index to keep track of deactivation 1: time to stay deactivated 2:index for activation 3:time for activation
  //could use a custom class but too lazy

  int [][]hoverList = new int [10000][2];
  int hoverLast=0;
  int hoverDone=0;
  int iterations=0;
  int commonsquares=0;
  boolean stable=false;//true
  boolean stable2=false; // changed 
  Random rand = new Random();
  int[] ruleset = {0,0,0,1,1,1,1,0};
  int fixd = 0;


  //UI items
  private TextField rowTf, colTf, ruleTf, actTf, deactTf;
  private Button stiniBtn,fastBtn,slowBtn,twoButton;
  private Panel buttonPanel;
  private Label prompt, rowPrompt, colPrompt, cellPrompt, iterationPrompt, partPrompt, statusPrompt, rulePrompt, actPrompt, deactPrompt, commonsquaresPrompt, boundPrompt;
  private Checkbox hoverButton, humanButton;
  public Choice iniCondList, iniCondList_comp, modeChoice, boundChoice;; // changed 


  /** Set up an off-screen Image for double-buffering */
  public void init() {
    //******for graphics**************

    addMouseListener(this);
    addMouseMotionListener(this);
    size = this.size();
    buffer = this.createImage(size.width, size.height);    
    
    bufferGraphics = buffer.getGraphics();
    background = new Color(255,255,255); //white background
    deadCol=new Color(180,255,206);
    liveCol=new Color(60,179,113);
    setBackground(background);

    rowTf = new TextField("64");
    colTf = new TextField("64");
    actTf = new TextField("10");
    deactTf = new TextField("20");
    ruleTf = new TextField("30");

    stiniBtn=new Button("Restart");
    stiniBtn.addActionListener(this);

    fastBtn=new Button("Faster");
    fastBtn.addActionListener(this);

    slowBtn=new Button("Slower");
    slowBtn.addActionListener(this);
	
	  twoButton =new Button("Two");
    twoButton.addActionListener(this);
	
	

    prompt = new Label("");
    rowPrompt = new Label("# of Rows:");
    colPrompt = new Label("# of Cols:");
    cellPrompt = new Label("cell #:");
    rulePrompt = new Label("Rule for Elementary CA (from 0 to 255):");
    actPrompt = new Label("Activation Period");
    deactPrompt = new Label("Deactivation Period");
    iterationPrompt = new Label("# iterations:");
	  commonsquaresPrompt = new Label("# commonsquares:");
    partPrompt = new Label("Common Patterns");
    statusPrompt = new Label("Status: Evolving");
    boundPrompt = new Label("Boundary type:");


    hoverButton = new Checkbox("Hands of god",false);
    humanButton = new Checkbox("Human touch",false);
    //hoverButton.addActionListener(this);

    iniCondList = new Choice();
    iniCondList.addItem("Glider");
    iniCondList.addItem("LWSS");
    iniCondList.addItem("Pulsar");
    iniCondList.addItem("R-pentomino");
    iniCondList.addItem("Diehard");
    iniCondList.addItem("Acorn");
    iniCondList.addItem("Gosper glider gun");
    iniCondList.addItem("1 cell gun");
    iniCondList.addItem("Gun 2");
    iniCondList.addItem("Gun 3");

	  iniCondList_comp = new Choice();
    iniCondList_comp.addItem("Glider");
    iniCondList_comp.addItem("LWSS");
    iniCondList_comp.addItem("Pulsar");
    iniCondList_comp.addItem("R-pentomino");
    iniCondList_comp.addItem("Diehard");
    iniCondList_comp.addItem("Acorn");
    iniCondList_comp.addItem("Gosper glider gun");
    iniCondList_comp.addItem("1 cell gun");
    iniCondList_comp.addItem("Gun 2");
    iniCondList_comp.addItem("Gun 3");

    boundChoice = new Choice();
    boundChoice.addItem("Circular Boundary");
    boundChoice.addItem("Fixed Boundary");	
	
	
	
    modeChoice = new Choice();
    modeChoice.addItem("Game of Life");
    modeChoice.addItem("Spiral Wave");
    modeChoice.addItem("Elementary CA");
    modeChoice.addItem("Comparison");
	

    buttonPanel= new Panel();

    buttonPanel.setLayout( new GridLayout(20,3));
    buttonPanel.add(cellPrompt);
    buttonPanel.add(iterationPrompt);
	  //buttonPanel.add(commonsquaresPrompt);
    buttonPanel.add(modeChoice);
    buttonPanel.add(prompt);
    buttonPanel.add(prompt);
    buttonPanel.add(rowPrompt);
    buttonPanel.add(rowTf);
    buttonPanel.add(colPrompt);
    buttonPanel.add(colTf);
    buttonPanel.add(partPrompt);
    buttonPanel.add(iniCondList);
	  //buttonPanel.add(iniCondList_comp);
    buttonPanel.add(stiniBtn);
	  //buttonPanel.add(twoButton);
    buttonPanel.add(prompt);
    buttonPanel.add(statusPrompt);
    buttonPanel.add(prompt);
    buttonPanel.add(fastBtn);
    buttonPanel.add(slowBtn);
    buttonPanel.add(hoverButton);



    setLayout(new BorderLayout());
    add(buttonPanel,BorderLayout.LINE_END);
    
    //buttonPanel.add();

    //initial conditions


    //*****determine geometry of 2D world**************
    //use try
    col=Integer.parseInt(colTf.getText());
    row=Integer.parseInt(rowTf.getText());
    
    
    ruleset = convertToBinary(Integer.parseInt(ruleTf.getText()));

    now = new boolean [row][col];
    next = new boolean [row][col];
	 now2 = new boolean [row][col];
	 next2 = new boolean [row][col];
	


    deactiveMatrix=new boolean[row][col];
    mode2Properties= new int [row][col][4];
    

    if (((int)Math.floor((double)(size.width-col*space-100))/(double)(col))<((double)(size.height-row*space)/(double)(row)))
    {
      cellSize=(int)Math.floor((double)(size.width-col*space-100)/(double)(col));
    }
    else
    {
      cellSize=(int)Math.floor((double)(size.height-row*space)/(double)(row));
    }

    xstart=(size.width-100-(cellSize+space)*col)/2;//starting point for 2D board
    ystart=(size.height-(cellSize+space)*row)/2;//starting point for 2D board

    //initial iteration
    t=0;

    //initialise all cells as dead
      for (int i=0; i<row;i++)
      {
        for(int j=0;j<col;j++)
        {
          now[i][j]=false;
		  now2[i][j] = false;
          mode2Properties[i][j][0]=0;//index for deact
          mode2Properties[i][j][1]=0;//time to stay deact
          mode2Properties[i][j][2]=0;//index for act
          mode2Properties[i][j][3]=actDuration;//time to stay act
        }
      }

      //activated cells
      int [][] ini= {{0,5},{1,6},{2,7},{3,8},{4,9},{5,10},{6,11},{7,12},{8,13},{9,14}};

      //deactivated cells
      int [][] ini2={{0,4},{1,5},{2,6},{3,7},{4,8},{5,9},{6,10},{7,11},{8,12},{9,13}};
      //initialise starting live cells
      //***************Gosper glider gun***********************
      //int [][] ini = {{14,2},{14,3},{15,2},{15,3},{12,15},{12,14},{13,13},{14,12},{15,12},{16,12},{17,13},{18,14},{18,15},{13,17},{14,18},{15,16},{15,18},{15,19},{16,18},{17,17},{10,26},{11,24},{11,26},{12,22},{12,23},{13,22},{13,23},{14,22},{14,23},{15,24},{15,26},{16,26},{12,36},{12,37},{13,36},{13,37}};
      
      //***************r-pentomino*************************
      //int [][]ini = {{row/2,col/2-1},{row/2+1,col/2+1},{row/2-1,col/2},{row/2,col/2},{row/2,col/2+1}};

      //***************other guns*********************
      //***************1 cell high gun****************
      //int ini[][]={{row/2,1},{row/2,2},{row/2,3},{row/2,4},{row/2,5},{row/2,6},{row/2,7},{row/2,8},{row/2,10},{row/2,11},{row/2,12},{row/2,13},{row/2,14},{row/2,18},{row/2,19},{row/2,20},{row/2,27},{row/2,28},{row/2,29},{row/2,30},{row/2,31},{row/2,32},{row/2,33},{row/2,35},{row/2,36},{row/2,37},{row/2,38},{row/2,39}};
      
      for (int i=0; i<ini.length;i++)
      {
        now[ini[i][0]][ini[i][1]]=true;
        mode2Properties[ini[i][0]][ini[i][1]][0]=1;
      }

	  for (int i=0; i<ini.length;i++)
      {
        now2[ini2[i][0]][ini2[i][1]]=true;
        mode2Properties[ini2[i][0]][ini2[i][1]][0]=1;
      }

	  
	  
	  
	  
      for (int i=0; i<ini2.length;i++)
      {
        //deactiveMatrix[ini[i][0]][ini[i][1]]=true;
        mode2Properties[ini[i][0]][ini[i][1]][0]=1;
        mode2Properties[ini[i][0]][ini[i][1]][1]=10;
      }
    
  }

  /** Draw the circle at its current position, using double-buffering */
  public void paint(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      // Draw into the off-screen buffer.
      bufferGraphics.setColor(background);
      // clear the buffer
      bufferGraphics.fillRect(0, 0, 6*size.width, 6*size.height); 
      
      //graphics for grid
      int kl=0;
      for (int i=0; i<row;i++)
      {
        for(int j=0;j<col;j++)
        {
          //kl++;
          bufferGraphics.drawString(""+fixd,10,30);
          if (now[i][j])
            bufferGraphics.setColor(liveCol);
          else
            bufferGraphics.setColor(deadCol);

          bufferGraphics.fillRect(xstart+(cellSize+space)*j,ystart+(cellSize+space)*i,cellSize,cellSize);//j i because (x,y) but by convention (rows,col)
        }
      }



      if(stini)
      {

        if(mode==2)
        {
      
          for (int i=0; i<row;i++)
          {
            for(int j=0;j<col;j++)
            {
              if (now[i][j])
                bufferGraphics.setColor(liveCol);
              else if (deactiveMatrix[i][j])
                bufferGraphics.setColor(Color.red);
              else
                bufferGraphics.setColor(deadCol);

              
              bufferGraphics.fillRect(xstart+(cellSize+space)*j,ystart+(cellSize+space)*i,cellSize,cellSize);//j i because (x,y) but by convention (rows,col)
            }
          }

          bufferGraphics.setColor(Color.black);
          bufferGraphics.drawString("Left CLick on Cell to deactivate the cell momentarily",10,20);
        }



        bufferGraphics.setColor(Color.black);
        bufferGraphics.drawString("Right CLick on Cell to make it Dead or Alive",10,10);

      
      }
	  
	  
	  
  	  if (twoTrue) 
      { 
  	  	   if(mode==4)
           {
        
            for (int i=0; i<row;i++)
            {
              for(int j=0;j<col;j++)
              {
                if (now[i][j] && !now2[i][j])
                  bufferGraphics.setColor(liveCol);
  			        else if (!now[i][j] && now2[i][j])
                  bufferGraphics.setColor(Color.blue);
  			        else if (now[i][j] && now2[i][j]) {
  				
  				      commonsquares++;
  				      commonsquaresPrompt.setText("common squares #"+commonsquares);
                bufferGraphics.setColor(Color.red);		
  					}
            else if (deactiveMatrix[i][j])
              bufferGraphics.setColor(Color.red);
            else
              bufferGraphics.setColor(deadCol);

                
              bufferGraphics.fillRect(xstart+(cellSize+space)*j,ystart+(cellSize+space)*i,cellSize,cellSize);//j i because (x,y) but by convention (rows,col)
            }
          }

          bufferGraphics.setColor(Color.black);
          bufferGraphics.drawString("Left CLick on Cell to deactivate the cell momentarily",10,20);
        }
   
      }

      bufferGraphics.setColor(Color.black);

      //bufferGraphics.drawString(msg,10,10);//debug
      
      g2.drawImage(buffer, 0, 0, this);

      //if (stini)
        //please_stop=true;
  }

  public void update(Graphics g) { paint(g); }

  /** The body of the animation thread */
  public void run() {
    while(!please_stop) {  

      if (mode==1)
      {
        //********************GOL RULES**************************************
        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            //getting the neighbours(gol = 8)
            next[i][j]=false;//clean up next array

            //int nn[][]={{i-1,j-1},{i-1,j},{i-1,j+1},{i,j-1},{i,j+1},{i+1,j-1},{i+1,j},{i+1,j+1}};
            int nn[][]={{mod(i-1,row),mod(j-1,col)},{mod(i-1,row),j},{mod(i-1,row),mod(j+1,col)},{i,mod(j-1,col)},{i,mod(j+1,col)},{mod(i+1,row),mod(j-1,col)},{mod(i+1,row),j},{mod(i+1,row),mod(j+1,col)}};

            //count alive
            int nalive=0;

            for (int r=0; r<nn.length;r++)
            {
              if (now[nn[r][0]][nn[r][1]])
              {
                nalive++;
              }
            }

            if(now[i][j])//alive rule
            {
              if ((nalive==2)||(nalive==3))
              {
                next[i][j]=true;
              }
            }
            else//dead rule
            {
              if (nalive==3)
              {
                next[i][j]=true;
              }
            }

          }
        }

        stable=true;//assume true until proven false
        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            if (now[i][j]!=next[i][j])
            {
              stable=false;
              now[i][j]=next[i][j];
            }
          }
        }

        int j=0;
        for (int i=hoverDone; i<hoverLast;i++)
        {
          now[hoverList[i][0]][hoverList[i][1]]=true;
          j++;
        }

        hoverDone=hoverDone+j;

        if (!stable)
        {
          iterations++;
          statusPrompt.setText("Status: Evolving");
        }
        else
          statusPrompt.setText("Status: Equlilibrium");

        
      }
      //*****************GOL***************************************
      else if (mode==2)
      {
      //*****************Spiral Waves******************************   
        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
              int nn[][]={{i+1,j},{i-1,j},{i,j+1},{i,j-1}};//getting NN

              boolean alive=false;

              //checking if any of the four neighbours are alive
              for (int r=0; r<nn.length;r++)
              {
                if(!((nn[r][0]<0)||(nn[r][1]<0)||(nn[r][0]>=row)||(nn[r][1]>=col)))
                {
                  if (now[nn[r][0]][nn[r][1]])
                  {
                    alive=true;
                    //break;//should?
                  }
                }
              }

              //making sure who is dead and alive
              if (humanButton.getState())
              {
                if ((mode2Properties[i][j][2]>0)&&(mode2Properties[i][j][2]<mode2Properties[i][j][3]))
                {
                  next[i][j]=true;
                  mode2Properties[i][j][2]++;
                }
                else if(mode2Properties[i][j][2]>mode2Properties[i][j][3])
                {
                  next[i][j]=false;
                  mode2Properties[i][j][2]=0;
                  mode2Properties[i][j][0]++;
                  mode2Properties[i][j][1]= rand.nextInt(2)+deactDuration-1;
                }
                else if((alive)&&(mode2Properties[i][j][0]==0))
                {
                  mode2Properties[i][j][2]++;
                  mode2Properties[i][j][3]= rand.nextInt(2)+actDuration-1;
                  next[i][j]=true;
                }
                else if(mode2Properties[i][j][0]>mode2Properties[i][j][1])
                {
                  mode2Properties[i][j][0]=0;
                  mode2Properties[i][j][1]=0;
                  next[i][j]=false;
                }
                else if(mode2Properties[i][j][0]>0)
                {
                  next[i][j]=false;
                  mode2Properties[i][j][0]++;
                }
              }
              else
              {
                if ((mode2Properties[i][j][2]>0)&&(mode2Properties[i][j][2]<mode2Properties[i][j][3]))
                {
                  next[i][j]=true;
                  mode2Properties[i][j][2]++;
                }
                else if(mode2Properties[i][j][2]>mode2Properties[i][j][3])
                {
                  next[i][j]=false;
                  mode2Properties[i][j][2]=0;
                  mode2Properties[i][j][0]++;
                  mode2Properties[i][j][1]= deactDuration;
                }
                else if((alive)&&(mode2Properties[i][j][0]==0))
                {
                  mode2Properties[i][j][2]++;
                  mode2Properties[i][j][3]= actDuration;
                  next[i][j]=true;
                }
                else if(mode2Properties[i][j][0]>mode2Properties[i][j][1])
                {
                  mode2Properties[i][j][0]=0;
                  mode2Properties[i][j][1]=0;
                  next[i][j]=false;
                }
                else if(mode2Properties[i][j][0]>0)
                {
                  next[i][j]=false;
                  mode2Properties[i][j][0]++;
                }
              }
              
          }
        }

        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            if (now[i][j]!=next[i][j])
            {
              now[i][j]=next[i][j];
            }
          }
        }

      iterations++;
      //***********************End Spiral**************************************
      }

      else if(mode == 3) {
        if(fixd == 0) {
        int r = iterations%(row);
        for(int i  = 0; i < col ; i++) {
          int left = now[r][(i+col-1)%col]?1:0;
          int it = now[r][i]?1:0;
          int right = now[r][(i+1)%col]?1:0;

        now[(iterations+1)%(row)][i] = getValue(left, it, right)==1?true:false;          


          }
        //System.out.println(iterations+"   "+ r);
          iterations++;
        }
        else
          {
        int r = iterations%(row);
        for(int i  = 1; i < col - 1; i++) {
          int left = now[r][i-1]?1:0;
          int it = now[r][i]?1:0;
          int right = now[r][i+1]?1:0;

        now[(iterations+1)%(row)][i] = getValue(left, it, right)==1?true:false;          


        }
        //System.out.println(iterations+"   "+ r);
        iterations++;
        }
      }
	  /////////paste here////////////////////////////////
	    if (mode==4)
      {
        //********************GOL RULES**************************************
        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            //getting the neighbours(gol = 8)
            next[i][j]=false;//clean up next array
			next2[i][j] = false;

            //int nn[][]={{i-1,j-1},{i-1,j},{i-1,j+1},{i,j-1},{i,j+1},{i+1,j-1},{i+1,j},{i+1,j+1}};
            int nn[][]={{mod(i-1,row),mod(j-1,col)},{mod(i-1,row),j},{mod(i-1,row),mod(j+1,col)},{i,mod(j-1,col)},{i,mod(j+1,col)},{mod(i+1,row),mod(j-1,col)},{mod(i+1,row),j},{mod(i+1,row),mod(j+1,col)}};

            //count alive
            int nalive=0;
			int nalive2=0;

            for (int r=0; r<nn.length;r++)
            {
              if (now[nn[r][0]][nn[r][1]])
              {
                nalive++;
              }
            }
			
			
		   for (int r=0; r<nn.length;r++)
            {
              if (now2[nn[r][0]][nn[r][1]])
              {
                nalive2++;
              }
            }

            if(now[i][j] && !now2[i][j])//alive rule
            {
              if ((nalive==2)||(nalive==3))
              {
                next[i][j]=true;
              }
            }
            else//dead rule
            {
              if (nalive==3)
              {
                next[i][j]=true;
              }
            }
			
			
		   if(now2[i][j] && !now[i][j])//alive rule
            {
              if ((nalive2==2)||(nalive2==3))
              {
                next2[i][j]=true;
              }
            }
            else//dead rule
            {
              if (nalive2==3)
              {
                next2[i][j]=true;
              }
            }

          }
        }

        stable=true;//assume true until proven false
        for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            if (now[i][j]!=next[i][j])
            {
              stable=false;
              now[i][j]=next[i][j];
            }
          }
        }
		
	  for (int i=0; i<row;i++)
        {
          for(int j=0;j<col;j++)
          {
            if (now2[i][j]!=next2[i][j])
            {
              stable2=false;
              now2[i][j]=next2[i][j];
            }
          }
        }

        int j=0;
        for (int i=hoverDone; i<hoverLast;i++)
        {
          now[hoverList[i][0]][hoverList[i][1]]=true;
          j++;
        }

        hoverDone=hoverDone+j;

		    iterations++;

      }
	  
	  
	  
	  /*if (mode==4) { 
	   iterations++;
	   System.out.println(iterations + " , " + commonsquares + "\n");
	  }*/
	  
	  
	  
	  
	  
	  
	  
	  
	  
      iterationPrompt.setText("iteration #"+iterations);

      repaint();

      try {
      Thread.sleep(speed);
      } catch (InterruptedException e) { ; }
    }
    animator = null;
  }

  /** Start the animation thread */
  public void start() {
    if (animator == null) {
      please_stop = false;
      animator = new Thread(this);
      animator.start();
    }
  }

  /** Stop the animation thread */
  public void stop() { please_stop = true; }

  //initializing cells with clicks
  /*public boolean mouseDown(Event e, int meex, int meey) 
  {
    //if (animator != null) please_stop = true;  // if running request a stop
    //else start();                              // otherwise start it.

    return(true);
  }*/

  public void mouseMoved(MouseEvent mee) {

    int meex=mee.getX();
    int meey=mee.getY();

    int numx=-10;
    int numy=-10;
    
    msg="NA";

    if ((meex>=xstart)&&(meex<(xstart+col*cellSize+col*space))&&(meey>=ystart)&&(meey<(ystart+row*cellSize+row*space)))
    {
      if(((meex-xstart)%(cellSize+space)<cellSize)&&((meey-ystart)%(cellSize+space)<cellSize))
      {
        numx = (meex-xstart)/(cellSize+space);
        numy = (meey-ystart)/(cellSize+space);
        msg="["+numy+","+numx+"]";//[r,c]

        if(hoverButton.getState())//hover selected!
        {
          try{
            hoverList[hoverLast][0]=numy;
            hoverList[hoverLast][1]=numx;
            hoverLast++;
          }catch (Exception e)//when over flow restart!
          {
            hoverLast=0;
            hoverDone=0;
            hoverList[hoverLast][0]=numy;
            hoverList[hoverLast][1]=numx;
            hoverLast++;
          }
        }
      }
    }

    cellPrompt.setText("cell #"+msg);
    //bufferGraphics.fillRect(xstart+(cellSize+space)*j,ystart+(cellSize+space)*i,cellSize,cellSize);//j i because (x,y) but by convention (rows,col)
    //return true;
  }

  public void mouseDragged(MouseEvent mee) {

  }


  //methods triggered by button action
  public void  actionPerformed(ActionEvent me)
  {
    if (me.getSource()==stiniBtn)
    {
      if(stini)//start is pressed
      {
        stini=false;
        //startScreen=false;
        stiniBtn.setLabel("Restart");
        //reinit();
        if (mode==2)
        {
          actDuration=Integer.parseInt(actTf.getText());
          deactDuration=Integer.parseInt(deactTf.getText());
        }
        else if (mode==3)
        { fixd = boundChoice.getSelectedIndex() ;
          ruleset = convertToBinary(Integer.parseInt(ruleTf.getText()));
        }
        start();
      }
      else//restart is pressed
      {
        stini=true;
        stiniBtn.setLabel("Start");
        please_stop=true;

        try {
        Thread.sleep(60);//to wait for other thread to realise stop was called
        } catch (InterruptedException e) { ; }

        //startScreen=true;
        reinit();
        repaint();
      }
    }
			
    if (me.getSource()==slowBtn)
    {
      speed=speed+10;
    }

    if (me.getSource()==fastBtn)
    {
      if (speed>10)
      {
        speed=speed-10;
      }
    }
  }

  //method triggered by item (mainly for droplist)
  public boolean action (Event evt, Object arg) {
    //reintiaiize when a different choice is made on the drop down list of either mode or initial conditons
    if((evt.target.equals(iniCondList))||(evt.target.equals(modeChoice)))
    {
      please_stop=true;
      stini=true;
      stiniBtn.setLabel("Start");
      reinit();
      repaint();
    } 
    else 
      return super.action(evt, arg);
    return true;
  }

  //customized modulus function (mainly for the negative modulo)
  public int mod (int a, int b)
  {
    int d=a%b;
    if (d<0)
      d=d+b;

    return(d);

  }

  //method to reinitialise board after RESTART button is clicked
  //prints the screen once to show selected pattern and stops to wait for the start button
  public void reinit () 
  {
    /*if(!stini)
    {*/

      //remove every single element 
    if(mode==1)
    {
      buttonPanel.remove(partPrompt);
      buttonPanel.remove(iniCondList);
      buttonPanel.remove(stiniBtn);
      buttonPanel.remove(prompt);
      buttonPanel.remove(statusPrompt);
      buttonPanel.remove(prompt);
      buttonPanel.remove(fastBtn);
      buttonPanel.remove(slowBtn);
      buttonPanel.remove(hoverButton);
    }
    else if (mode==2)
    {
      buttonPanel.remove(actPrompt);
      buttonPanel.remove(actTf);
      buttonPanel.remove(deactPrompt);
      buttonPanel.remove(deactTf);
      buttonPanel.remove(stiniBtn);
      buttonPanel.remove(prompt);
      buttonPanel.remove(prompt);
      buttonPanel.remove(humanButton);
      buttonPanel.remove(fastBtn);
      buttonPanel.remove(slowBtn);
    }
	
    else if(mode==3)
    {
      buttonPanel.remove(rulePrompt);
      buttonPanel.remove(ruleTf); 
      buttonPanel.remove(stiniBtn);
      buttonPanel.remove(prompt);
      buttonPanel.remove(prompt);
      buttonPanel.remove(fastBtn);
      buttonPanel.remove(slowBtn);
      buttonPanel.remove(ruleTf);
      buttonPanel.remove(boundPrompt);
    }
	
	else{
      buttonPanel.remove(commonsquaresPrompt);
      buttonPanel.remove(partPrompt);
      buttonPanel.remove(iniCondList);
      buttonPanel.remove(iniCondList_comp);
      buttonPanel.remove(stiniBtn);
      buttonPanel.remove(prompt);
      //buttonPanel.remove(statusPrompt);
      buttonPanel.remove(prompt);
      buttonPanel.remove(fastBtn);
      buttonPanel.remove(slowBtn);
      buttonPanel.remove(hoverButton);
    }
	


      hoverButton.setState(false);
      col=Integer.parseInt(colTf.getText());
      row=Integer.parseInt(rowTf.getText());

      now = new boolean [row][col];
      next = new boolean [row][col];
	    now2 = new boolean [row][col]; 
	    next2 = new boolean [row][col];
	  
      deactiveMatrix = new boolean [row][col];
      mode2Properties=new int [row][col][4];
      fixd = boundChoice.getSelectedIndex() ;

      iterations=0;
	    commonsquares=0;
      iterationPrompt.setText("iterations"+ iterations);
	    commonsquaresPrompt.setText("common sq"+ commonsquares);
      stable=false;
      statusPrompt.setText("Status: Evolving");
      //selectMatrix = new boolean [row][col];

      if (((int)Math.floor((double)(size.width-col-100))/(double)(col))<((double)(size.height-row)/(double)(row)))
      {
        cellSize=(int)Math.floor((double)(size.width-col-100)/(double)(col));
      }
      else
      {
        cellSize=(int)Math.floor((double)(size.height-row)/(double)(row));
      }

      xstart=(size.width-100-(cellSize+1)*col)/2;//starting point for 2D board
      ystart=(size.height-(cellSize+1)*row)/2;//starting point for 2D board
    //}

    //should automate this
    //int [][]ini = {{row/2,col/2-1},{row/2+1,col/2+1},{row/2-1,col/2},{row/2,col/2},{row/2,col/2+1}};
    //int [][] ini = {{14,2},{14,3},{15,2},{15,3},{12,15},{12,14},{13,13},{14,12},{15,12},{16,12},{17,13},{18,14},{18,15},{13,17},{14,18},{15,16},{15,18},{15,19},{16,18},{17,17},{10,26},{11,24},{11,26},{12,22},{12,23},{13,22},{13,23},{14,22},{14,23},{15,24},{15,26},{16,26},{12,36},{12,37},{13,36},{13,37}};
    //initial iteration
    //int [][]ini={{0,0}};


    int [][]ini=setIni(iniCondList.getSelectedIndex());
	  int [][]ini2=setIni(iniCondList_comp.getSelectedIndex());
    
    int prevMode=mode;
    mode=modeChoice.getSelectedIndex()+1;//change mode

    t=0;

    //initialise all cells as dead
    for (int i=0; i<row;i++)
    {
      for(int j=0;j<col;j++)
     {
        now[i][j]=false;
		    now2[i][j]=false;
        deactiveMatrix[i][j]=false;
        mode2Properties[i][j][0]=0;
        mode2Properties[i][j][1]=0;
        mode2Properties[i][j][2]=0;
        mode2Properties[i][j][3]=0;
      }
    }

    if(mode ==1) 
    {
      for (int i=0; i<ini.length;i++)
      {
         now[ini[i][0]][ini[i][1]]=true;
      }
        
      buttonPanel.add(partPrompt);
      buttonPanel.add(iniCondList);
      buttonPanel.add(stiniBtn);
      buttonPanel.add(prompt);
      buttonPanel.add(statusPrompt);
      buttonPanel.add(prompt);
      buttonPanel.add(fastBtn);
      buttonPanel.add(slowBtn);
      buttonPanel.add(hoverButton);
      //buttonPanel.add(slowBtn);
    }
    else if(mode ==2) 
    {

            //activated cells
      int [][] a= {{0,5},{1,5},{2,5},{3,5},{4,5}};

      //deactivated cells
      int [][] b={{0,6},{1,6},{2,6},{3,6},{4,6}};
      
      for (int i=0; i<a.length;i++)
      {
         now[a[i][0]][a[i][1]]=true;
         mode2Properties[a[i][0]][a[i][1]][2]=1;
         mode2Properties[a[i][0]][a[i][1]][3]=rand.nextInt(2)+actDuration-1;
      }

      for (int i=0; i<b.length;i++)
      {
         deactiveMatrix[b[i][0]][b[i][1]]=true;
         mode2Properties[b[i][0]][b[i][1]][0]=1;
         mode2Properties[b[i][0]][b[i][1]][1]=rand.nextInt(2)+deactDuration-1;
      }
      
      buttonPanel.add(actPrompt);
      buttonPanel.add(actTf);
      buttonPanel.add(deactPrompt);
      buttonPanel.add(deactTf);
      buttonPanel.add(stiniBtn);
      buttonPanel.add(prompt);
      buttonPanel.add(prompt);
      buttonPanel.add(humanButton);
      buttonPanel.add(fastBtn);
      buttonPanel.add(slowBtn);
    }
    else if(mode == 3)
    {
      now[0][col/2] = true;
      buttonPanel.add(rulePrompt);
      buttonPanel.add(ruleTf);
      buttonPanel.add(boundPrompt);
      buttonPanel.add(boundChoice); 
      buttonPanel.add(stiniBtn);
      buttonPanel.add(prompt);
      buttonPanel.add(prompt);
      buttonPanel.add(fastBtn);
      buttonPanel.add(slowBtn);

    }
	
   else
    {
	  for (int i=0; i<ini.length;i++)
      {
         now[ini[i][0]][ini[i][1]]=true;
      }
     for (int i=0; i<ini2.length;i++)
      {
         now2[ini2[i][0]][ini2[i][1]]=true;
      }

      twoTrue=true;
      buttonPanel.add(commonsquaresPrompt);
      buttonPanel.add(partPrompt);
      buttonPanel.add(iniCondList);
      buttonPanel.add(iniCondList_comp);
      buttonPanel.add(stiniBtn);
      buttonPanel.add(prompt);
      //buttonPanel.add(statusPrompt);
      buttonPanel.add(prompt);
      buttonPanel.add(fastBtn);
      buttonPanel.add(slowBtn);
      buttonPanel.add(hoverButton);
    }

    //buttonPanel.revalidate();
    buttonPanel.revalidate();
    validate();

  }

  //method to refresh the graphics in the initialize screen after running reint
  //refreshes based on the clicks cells to activate and deactivate
  public void refresh () 
  {
      for (int i=0; i<row;i++)
      {
        for(int j=0;j<col;j++)
        {
          next[i][j]=now[i][j];
		      next2[i][j]=now2[i][j];
        }
      }

      repaint();
  }

  //selecting the correct initial cell conditions based on selected drop down list option
  public int[][] setIni(int iniCondIndex)
  {
    if(iniCondIndex==0) //glider
    {
      int [][] sub = {{1,0},{2,1},{2,2},{1,2},{0,2}};
      return(sub);
    }
    else if (iniCondIndex==1) //LWSS
    {
      int [][] sub = {{0,0},{0,3},{1,4},{2,0},{2,4},{3,1},{3,2},{3,3},{3,4}};
      return(sub);
    }
    else if (iniCondIndex==2) //Pulsar
    {
      int [][] sub = {{0,0},{0,3},{1,4},{2,0},{2,4},{3,1},{3,2},{3,3},{3,4}};
      return(sub);//lazy
    }
    else if (iniCondIndex==3) //R-pentomino
    {
      int [][] sub = {{row/2,col/2-1},{row/2+1,col/2+1},{row/2-1,col/2},{row/2,col/2},{row/2,col/2+1}};
      return(sub);
    }
    else if (iniCondIndex==4) //Diehard
    {
      int [][] sub = {{row/2+1,col/2-2},{row/2,col/2-2},{row/2,col/2-3},{row/2-1,col/2+3},{row/2+1,col/2+2},{row/2+1,col/2+3},{row/2+1,col/2+4}};
      return(sub);
    }
    else if (iniCondIndex==5) //Acorn
    {
      int [][] sub = {{row/2,col/2},{row/2-1,col/2-2},{row/2+1,col/2-3},{row/2+1,col/2-2},{row/2+1,col/2+1},{row/2+1,col/2+2},{row/2+1,col/2+3}};
      return(sub);
    }
    else if (iniCondIndex==6) //Gosper Gun
    {
      int [][] sub =  {{14,2},{14,3},{15,2},{15,3},{12,15},{12,14},{13,13},{14,12},{15,12},{16,12},{17,13},{18,14},{18,15},{13,17},{14,18},{15,16},{15,18},{15,19},{16,18},{17,17},{10,26},{11,24},{11,26},{12,22},{12,23},{13,22},{13,23},{14,22},{14,23},{15,24},{15,26},{16,26},{12,36},{12,37},{13,36},{13,37}};
      return(sub);
    }
    else if (iniCondIndex==7) //1 cell Gun
    {
      //int sub[][]={{1,row/2},{2,row/2},{3,row/2},{4,row/2},{5,row/2},{6,row/2},{7,row/2},{8,row/2},{10,row/2},{11,row/2},{12,row/2},{13,row/2},{14,row/2},{18,row/2},{19,row/2},{20,row/2},{27,row/2},{28,row/2},{29,row/2},{30,row/2},{31,row/2},{32,row/2},{33,row/2},{35,row/2},{36,row/2},{37,row/2},{38,row/2},{39,row/2}};
      int sub[][]={{row/2,1},{row/2,2},{row/2,3},{row/2,4},{row/2,5},{row/2,6},{row/2,7},{row/2,8},{row/2,10},{row/2,11},{row/2,12},{row/2,13},{row/2,14},{row/2,18},{row/2,19},{row/2,20},{row/2,27},{row/2,28},{row/2,29},{row/2,30},{row/2,31},{row/2,32},{row/2,33},{row/2,35},{row/2,36},{row/2,37},{row/2,38},{row/2,39}};
      return(sub);
    }
    else if (iniCondIndex==8) //gun2
    {
      int sub[][]={{row/2,1}};//lazy to input
      return(sub);
    }
    else if (iniCondIndex==9) //gun3
    {
      int sub[][]={{row/2,1}};//lazy to input
      return(sub);
    }

    /*iniCondList = new Choice();
    iniCondList.addItem("Glider");
    iniCondList.addItem("LWSS");
    iniCondList.addItem("Pulsar");
    iniCondList.addItem("R-pentomino");
    iniCondList.addItem("Diehard");
    iniCondList.addItem("Acorn");
    iniCondList.addItem("Gosper glider gun");
    iniCondList.addItem("Gun 1");
    iniCondList.addItem("Gun 2");
    iniCondList.addItem("Gun 3");*/
    return(null);

  }

  //*****************MouseListener Events**********************************
    public void mouseClicked(MouseEvent e)
    {
      int meex=e.getX();
      int meey=e.getY();

      //msg=""+e;//debugg

      //function enable users to make a cell alive or dead
      if (stini)//for click events that happens after restart button is clicked but before start button is click
      {
        if ((meex>=xstart)&&(meex<(xstart+col*cellSize+col*space))&&(meey>=ystart)&&(meey<(ystart+row*cellSize+row*space)))
        {
            int numx = (meex-xstart)/(cellSize+space);
            int numy = (meey-ystart)/(cellSize+space);
            msg="["+numy+","+numx+"]";//[r,c]

            if (e.getButton() == MouseEvent.BUTTON1)//includes spiral and gol
            {
              if(now[numy][numx])//cell neutralized
              {
                now[numy][numx]=false;
                deactiveMatrix[numy][numx]=false;
                mode2Properties[numy][numy][0]=0;
                mode2Properties[numy][numx][1]=0;
                mode2Properties[numy][numx][2]=0;
                mode2Properties[numy][numx][3]=0;
              }
              else// cell activated
              {
                now[numy][numx]=true;
                deactiveMatrix[numy][numx]=false;
                mode2Properties[numy][numx][0]=0;
                mode2Properties[numy][numx][1]=0;
                mode2Properties[numy][numx][2]=1;
                mode2Properties[numy][numx][3]=rand.nextInt(2)+actDuration-1;
              }
            }
            else
            {
              if(mode==2)// for spiral wave rules
              {
                if(deactiveMatrix[numy][numx])//cell neutralized
                {
                  deactiveMatrix[numy][numx]=false;
                  mode2Properties[numy][numx][0]=0;
                  mode2Properties[numy][numx][1]=0;
                  mode2Properties[numy][numx][2]=0;
                  mode2Properties[numy][numx][3]=0;
                }
                else//cell deactivated
                {
                  deactiveMatrix[numy][numx]=true;
                  now[numy][numx]=false;
                  mode2Properties[numy][numx][0]=1;
                  mode2Properties[numy][numx][1]=rand.nextInt(2)+deactDuration-1;
                  mode2Properties[numy][numx][2]=0;
                  mode2Properties[numy][numx][3]=0;
                }
              }
            }
        }

        refresh();
      }
      else
      {
        if (animator != null) please_stop = true;  // if running request a stop
        else start();                              // otherwise start it.
      }
    }

    public int getValue(int a, int b, int c) {
      
      int index = a * 4 + b * 2+ c * 1;
      return ruleset[index];
    }

    public int[] convertToBinary(int dec) {
      int[] bits = new int[8];
      //System.out.println(dec+":");
      for (int i = 7; i >= 0; i--) {
          bits[i] = ((dec & (1 << i)) != 0)?1:0;
          //System.out.print(bits[i]);
      }
      //System.out.println("");  
      return bits;
    }
    public void  mouseEntered(MouseEvent e){}

    public void  mouseExited(MouseEvent e){}

    public void  mousePressed(MouseEvent e){}

    public void  mouseReleased(MouseEvent e){}
}