function draw_2bar(x,y,y1,y2,title1,outFile)
figure;

% bar(x,[y1 y2],'BarWidth',1);
% ax = get(gca);
% cat = ax.Children;
% 
% %set the first bar chart style
% set(cat(2),'FaceColor',[145 25 206]/255,'BarWidth',2);
% 
% %set the second bar chart style
% set(cat(1),'FaceColor',[45 125 206]/255,'BarWidth',2);
z = [y1 y2];
bar(z)
title(title1);
ylabel('The average routing path length (Hops)');
set(gca,'yscale','linear');
xlabel('Number of arrived demands');
set(gca,'XTickLabel',y);

legend('RBP algorithn','ProvisionTraffic algorithm','Location','Best');
legend1 = legend(gca,'show');
set(legend1,'YColor',[1 1 1],'XColor',[1 1 1]);

set(gca,'box','off');
im_hatch = applyhatch_pluscolor(gcf,'\.+|-/',0,[0 0 1 1 0 0],[],200,3,2);

imwrite(im_hatch,'delay.jpg','jpg');
%set the axes style

%grid

saveas(gcf,outFile)