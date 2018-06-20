
[num,txt,raw] = xlsread('multiResult.xlsx');
title3s={ 'Scenario 2, n=15' 'Scenario 3, n=50' 'Scenario 4, n=200'};
outFile1={'delay1.jpg' 'delay2.jpg' 'delay3.jpg' };
outFile2={'accept1.jpg' 'accept2.jpg' 'accept3.jpg'};
outFile3={'nfv1.jpg' 'nfv2.jpg' 'nfv3.jpg'};
for n=0:3
    t1=n*8+2;
    t2=t1+7;
    x=[1:1:8];
    y=num(t1:t2,3);
    y1=num(t1:t2,29);
    y2=num(t1:t2,9);
    y3=num(t1:t2,30);
    y4=num(t1:t2,10);
    y5=num(t1:t2,28);
    y6=num(t1:t2,8);
    draw_2line(x,y,y1,y2,title3s{n+1},outFile2{n+1});
    draw_2line_NFVcost(x,y,y5,y6,title3s{n+1},outFile3{n+1});
    draw_2bar(x,y,y3,y4,title3s{n+1},outFile1{n+1});
end
