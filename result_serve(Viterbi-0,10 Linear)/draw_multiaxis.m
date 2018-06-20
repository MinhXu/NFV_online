function draw_multiaxis(y,y1,y2,y3,y4,outFile)
figure;
[ax,p1,p2] = plotyy(y,y1,y,y2);
set(p1,'LineWidth',4.0);
set(p1,'Color','r');
set(p1,'Marker','d');
set(p1,'MarkerSize',7.0);
set(p2,'LineWidth',4.0);
set(p2,'Color','g');
set(p2,'Marker','^');
set(p2,'MarkerSize',7.0);
xlabel('Migratiion Percentage (%)') % label x-axis
ylabel(ax(1),'U_n(t)+U_l(t)+U_{penalty}($)') % label left y-axis
set(ax(1),'Ylim',[1000 1500])
set(ax(2),'Ylim',[1000 2300])
set(ax,{'ycolor'},{'r';'g'})
ylabel(ax(2),'U_n(t)+U_l(t)+U_{penalty}($)') % label right y-axis

axes(ax(1))
hold on
[p3]=plot(y,y3);
set(p3,'LineWidth',4.0);
set(p3,'Color','r');
set(p3,'Marker','d');
set(p3,'MarkerSize',7.0);
set(p3,'LineStyle',':');
axes(ax(2))
hold on
[p4]=plot(y,y4);
set(p4,'LineWidth',4.0);
set(p4,'Color','g');
set(p4,'Marker','^');
set(p4,'MarkerSize',7.0);
set(p4,'LineStyle',':');
M=[p1 p3 p2 p4];
legend(M,'RBP algorithn (Linear)','RBP algorithn (Sqrt)','ProvisionTraffic algorithm (Linear)','ProvisionTraffic algorithm (Sqrt)','Location','Best');
%legend1 = legend(gca,'show');
%set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);
%set(gcf,'PaperPositionMode','auto')
fig = gcf;
fig.PaperUnits = 'inches';
fig.PaperPosition = [0 0 1 1];
%fig.PaperSize = [7 9];
%fig.PaperPositionMode = 'auto';
saveas(gcf,outFile)