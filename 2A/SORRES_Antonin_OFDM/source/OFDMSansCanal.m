clear all
close all

%Paramètres
N=16;%Nombre de sous porteuses
N_bits_port=10000; %Nombre de bits par porteuse
N_bits=N*N_bits_port; %Nombre de bits totales
port_actives=2; %Nombre de porteuse actives

% Génération des symboles 
X = zeros(N,N_bits_port);

%Pour le cas 1 et 2 (il faut commenter l'autre boucle
% for i=1:port_actives
%     bits=randi([0 1],1,N_bits_port);
%     symboles=2*bits-1;
%     X(i,:)=symboles;
% end

%Pour le cas 3 (il faut commenter l'autre boucle)
for i=4:N-4
   bits=randi([0 1],1,N_bits_port);
   symboles=2*bits-1;
   X(i,:)=symboles;
end

%filtrage
Xe=ifft(X,N);
Y=reshape(Xe,1,N_bits);

%DSP 
%[dsp,f] = pwelch(Y,[],[],[],N,'centered');%cas 1 et 2
[dsp,f]= pwelch(Y,[],[],[],N,'twosided');%cas 3

%Démodulation
Ybis=reshape(Y,16,[]);
Xbis=fft(Ybis,N);

%Calcul TEB
TEB=sum(sum(X~=round(Xbis)))/N_bits;
fprintf('Le TEB simulé sans canal est de %d\n',TEB)

%Affichage de la DSP
figure('Name','DSP en fonction du nombre de porteuses actives');
nexttile
plot(f,10*log(dsp))
xlabel('fréquence')
ylabel('DSP')