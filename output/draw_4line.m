function draw_4line(x,y,y1,y2,y3,y4,title1,outFile)
figure;
plot(x, y1, 'rd-','MarkerFaceColor','r','LineWidth',4.0,'MarkerSize',7.0);
%plot(x, y1,'LineWidth',2.0,'MarkerSize',6.0,'Marker','diamond');
hold on;
plot(x, y2, 'g^-','MarkerFaceColor','g','LineWidth',4.0,'MarkerSize',7.0);
%plot(x, y2, 'LineWidth',2.0,'MarkerSize',3.0,'Marker','^');
hold on;
plot(x, y3, 'r:d','MarkerFaceColor','r','LineWidth',4.0,'MarkerSize',7.0);
hold on;
plot(x, y4, 'g:^','MarkerFaceColor','g','LineWidth',4.0,'MarkerSize',7.0);
%title(title1);
ylabel('The Acceptance Ratio');
set(gca,'yscale','linear');
xlabel('Number of arrived demands');
set(gca,'XTickLabel',y);
legend('RBP algorithm (BCube)','ProvisionTraffic algorithm (BCube)','RBP algorithm (FatTree)','ProvisionTrafic algorithm (FatTree)','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);

%set(gcf,'units','inches','position',[0,0,420,230])
%set(gcf,'PaperPositionMode','auto')
fig = gcf;
fig.PaperUnits = 'inches';
fig.PaperPosition = [0 0 1 1];
%fig.PaperPositionMode = 'manual';
%grid

saveas(gcf,outFile)
