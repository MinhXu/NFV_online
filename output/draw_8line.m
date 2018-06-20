function draw_8line(x,y,y1,y2,y3,y4,y5,y6,y7,y8,title1,outFile)
figure;
plot(x, y1);
hold on;
plot(x, y2);
hold on;
plot(x, y3);
hold on;
plot(x, y4);
hold on;
plot(x, y5);
hold on;
plot(x, y6);
hold on;
plot(x, y7);
hold on;
plot(x, y8);
title(title1);
ylabel('The accept ratio');
set(gca,'yscale','linear');
xlabel('Number of demands');
set(gca,'XTickLabel',y);
legend('Test1(d=20)','Test2(d=20)','Test3(d=20)','Test4(d=20)','Test5(d=20)','Test6(d=20)','Test7(d=20)','Test8(d=20)','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);

%set(gcf,'units','inches','position',[0,0,420,230])
set(gcf,'PaperPositionMode','auto')
fig = gcf;
fig.PaperUnits = 'inches';
fig.PaperPosition = [0 0 5 3];
fig.PaperPositionMode = 'manual';
%grid

saveas(gcf,outFile)
