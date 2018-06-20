
[num,txt,raw] = xlsread('testMigration.xlsx');
    x=[0:1:10];
    y=[0:10:100];
    %y=num(t1:t2,3);   
    y1=num(1,5:15);
    y2=num(2,5:15);
    y3=num(3,5:15);
    y4=num(4,5:15);
    ymin1=1150;
    ymax1=1550;
    ymin2=1200;
    ymax2=2300;
    ymin3=1000;
    ymax3=1250;
    ymin4=1300;
    ymax4=1800;
    %draw_2line(x,y,y1,y2,'Linear Penalty Costs','linear.jpg',ymin1,ymax1,ymin2,ymax2);
    %draw_2line(x,y,y3,y4,'Square root Penalty Costs','sqrt.jpg',ymin3,ymax3,ymin4,ymax4);
    draw_multiaxis(y,y4,y2,y3,y1,'migrationPer.jpg')
