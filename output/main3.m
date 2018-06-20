
[num,txt,raw] = xlsread('multiResult2106.xlsx');

    x=[1:1:8];
    y=num(1:8,3);
    y1=num(1:8,33);
    y2=num(1:8,13);
    y3=num(11:18,33);
    y4=num(11:18,13);
    y5=num(21:28,33);
    y6=num(21:28,13);
    y7=num(32:39,33);
    y8=num(32:39,13);
    draw_4line(x,y,y1,y2,y5,y6,'BCube(n=2,k=2)','topo1.jpg');
    draw_4line(x,y,y3,y4,y7,y8,'BCube(n=24,k=2)','topo2.jpg');
%     draw_2line(x,y,y1,y2,'BCube(n=2,k=2)','Bcube2-2.jpg');
%     draw_2line(x,y,y3,y4,'BCube(n=4,k=2)','Bcube4-2.jpg');
%     draw_2line(x,y,y5,y6,'FatTree(k=4)','FatTree4.jpg');
%     draw_2line(x,y,y7,y8,'FatTree(k=6)','FatTree6.jpg');
    