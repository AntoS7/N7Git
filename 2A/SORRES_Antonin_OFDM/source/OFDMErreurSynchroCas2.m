clear all
close all

%réponse impulsionnelle
h=[0.407,0.815,0.407];
figure('Name','Réponse en fréquence du canal de propagation');
freqz(h,1024,'whole')
grid on;



%Constantes
N=16;%Nombre de sous porteuses

N_bits=10000;
port_actives=16;
interPrefixe=2*3;
erreursynch=2;%au milieu du préfixe cyclique 

%Mapping 
X = zeros(N,N_bits);
for i=1:port_actives
    bits=randi([0 1],1,N_bits);
    symboles=2*bits-1;
    X(i,:)=symboles';
end

%modulateur OFDM
Xe=ifft(X,N);

%Préfixe cyclique
Xepref=zeros(interPrefixe+N,N_bits);
Xepref(interPrefixe+1:end,:)= Xe;
Xepref(1:interPrefixe,:)=Xe(end-interPrefixe+1:end,:);

Ypref=reshape(Xepref,1,[]);

%filtre
SignalSortieCanal=filter(h,1,Ypref);

dsp = pwelch(Ypref,[],[],[],N);

dspSortieCanal = pwelch(SignalSortieCanal,[],[],[],N);

figure('Name','DSP');
plot(10*log(dsp))
hold on
plot(10*log(dspSortieCanal))
grid
legend('DSPEntreeCanal','DSPSortieCanal')

%Enlever l'intervalle de garde
SignalSortiebisGarde=reshape(SignalSortieCanal,size(Xepref));
%erreur de synchro
SignalSortiebis=SignalSortiebisGarde(erreursynch+1:N+erreursynch,:);

%Démodulation 

SignalSortieDemodule=fft(SignalSortiebis,N);

%ZFE
Ck=fft(h,N);
H=repmat(Ck(:),1,N_bits);
SignalSortieDemodEga=(1./H).*SignalSortieDemodule;

%Constellations
ConstPorteuse6=SignalSortieDemodEga(6,:);
ConstPorteuse15=SignalSortieDemodEga(15,:);

figure('Name','Constellations porteuses')
subplot(2,1,1)
scatter(real(ConstPorteuse6),imag(ConstPorteuse6))
title('Constellation porteuse 6')
grid on
subplot(2,1,2)
scatter(real(ConstPorteuse15),imag(ConstPorteuse15))
title('Constellation porteuse 15')
grid on


Xrecepbin = SignalSortieDemodEga;
Xrecepbin(real(SignalSortieDemodEga)<0)=-1;
Xrecepbin(real(SignalSortieDemodEga)>0)=1;
%teb pas bon tant qu'il n'y a pas d'égalisation
TEB=mean(X~=Xrecepbin,"all")