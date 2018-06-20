% Create some data
[num,txt,raw] = xlsread('multiResult.xlsx');
title1s={'Scenario 2, n=15' 'Scenario 3, n=50' 'Scenario 4, n=200'};
outFile1={'5l_scenrio1.jpg' '5l_scenrio2.jpg' '5l_scenrio3.jpg' };
outFile2={'8l_scenrio1' '8l_scenrio2.jpg' '8l_scenrio3.jpg'};
outFile3={'migration1.jpg' 'migration2.jpg' 'migration3.jpg' };


for n=0:3
    t1=n*8+2;
    t2=t1+7;
    x=[1:1:8];
    %x=num(t1:t2,2);
    %y=[1:1:8];
    y=num(t1:t2,3);
    y1=num(t1:t2,8);
    y2=num(t1:t2,12);
    y3=num(t1:t2,16);
    y4=num(t1:t2,20);
    y5=num(t1:t2,24);
    y6=num(t1:t2,28);
    y7=num(t1:t2,32);
    y8=num(t1:t2,8);
    y9=num(t1:t2,16);
    y10=num(t1:t2,12);
    y11=num(t1:t2,5);
    y12=num(t1:t2,7);
    y13=num(t1:t2,17);
     y14=num(t1:t2,45);
    y15=num(t1:t2,46);
    y16=num(t1:t2,47);
    y17=num(t1:t2,48);
    %draw_5line(x,y,y1,y2,y3,y4,y5,title1s{n+1},outFile1{n+1});
    draw_4line(x,y,y14,y15,y16,y17,title1s{n+1},outFile3{n+1});
    %draw_8line(x,y,y6,y7,y8,y9,y10,y11,y12,y13,title1s{n+1},outFile2{n+1});
end