function draw_4bar(x,y1,y2,y3,y4,outFile)
figure;
%bar(x,[y1 y2 y3],'BarWidth',1);

y = [y1;y2;y3;y4];
bar(y)
% ax = get(gca);
% cat = ax.Children;
% set(cat(3),'FaceColor','y','BarWidth',2);
% set(cat(2),'FaceColor','m','BarWidth',2);
% set(cat(1),'FaceColor','c','BarWidth',2);
ylabel('Distribution of VNFs (%) ');
ylim(gca,[0 100]);
set(gca,'yscale','linear');
set(gca,'XTick',[1 2 3 4])
set(gca,'XTickLabel',{'Max-Min Alg','Min-Min Alg','ProvisionTraffic Alg','Random Alg'});
legend('Core Layer','Aggregation Layer','Access Layer','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);
im_hatch = applyhatch_pluscolor(gcf,'\.+|-/',0,[0 0 1 1 0 0],[],200,3,2);

imwrite(im_hatch,'distribution.jpg','jpg');
%applyhatch_plusC(gcf, '\-x', 'rkb');

ylabel('Distribution of VNFs (%) ');
ylim(gca,[0 100]);
set(gca,'yscale','linear');
set(gca,'XTick',[1 2 3])
set(gca,'XTickLabel',{'Max-Min Alg','Min-Min Alg','ProvisionTraffic Alg','Random Alg'});
legend('Core Layer','Aggregation Layer','Access Layer','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);

set(gca,'box','off');
saveas(gcf,outFile)