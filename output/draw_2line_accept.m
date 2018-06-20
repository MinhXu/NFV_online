function draw_2line_accept(x,y,y1,y2,title1,outFile)
figure;

plot(x, y1, 'rd-','MarkerFaceColor','r','LineWidth',4.0,'MarkerSize',7.0);
%plot(x, y1,'LineWidth',2.0,'MarkerSize',6.0,'Marker','diamond');
hold on;
plot(x, y2, 'g^-','MarkerFaceColor','g','LineWidth',4.0,'MarkerSize',7.0);

title(title1,'FontSize',14);
ylabel('The acceptance ratio','FontSize',14);
set(gca,'yscale','linear');
xlabel('Number of arrived demands','FontSize',14);
set(gca,'XTickLabel',y);
legend('RBP algorithn','ProvisionTraffic algorithm','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);
ylim(gca,[0 1]);
%set(gcf,'units','inches','position',[0,0,420,230])
%set(gcf,'PaperPositionMode','auto')
fig = gcf;
fig.PaperUnits = 'inches';
fig.PaperPosition = [0 0 1 1];
%fig.PaperPositionMode = 'manual';
%grid

saveas(gcf,outFile)
