function draw_5line(x,y,y1,y2,y3,y4,y5,title1,outFile)
figure;
plot(x, y1,'r');
hold on;
plot(x, y2,'g');
hold on;
plot(x, y3,'b');
hold on;
plot(x, y4,'k');
hold on;
plot(x, y5,'y');
title(title1);
ylabel('The accept ratio');
set(gca,'yscale','linear');
xlabel('Number of demands');
set(gca,'XTickLabel',y);
legend('0%','30%','50%','70%','100%','Location','Best');
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
