function draw_2line(x,y,y1,y2,title1,outFile,ymin1,ymax1,ymin2,ymax2)
figure;
[ax,p1,p2] = plotyy(y,y1,y,y2);
set(p1,'LineWidth',2.0);
set(p1,'Color','r');
set(p1,'Marker','d');
set(p1,'MarkerSize',4.0);
set(p2,'LineWidth',2.0);
set(p2,'Color','g');
set(p2,'Marker','^');
set(p2,'MarkerSize',4.0);
xlabel('Migratiion Percentage (%)') % label x-axis
%set(ax,'XTickLabel',y);
ylabel(ax(1),'NFV cost ($)') % label left y-axis
set(ax(1),'Ylim',[ymin1 ymax1])
set(ax(2),'Ylim',[ymin2 ymax2])
set(ax,{'ycolor'},{'r';'g'})
%title(title1);
ylabel(ax(2),'NFV cost ($)') % label right y-axis

legend('RBP algorithn','ProvisionTraffic algorithm','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);
%set(gcf,'PaperPositionMode','auto')
fig = gcf;
fig.PaperUnits = 'inches';
fig.PaperPosition = [0 0 1 1];
%fig.PaperPositionMode = 'manual';
saveas(gcf,outFile)