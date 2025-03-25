clear all
close all

%réponse impulsionnelle
h=[0.407,0.815,0.407];
figure('Name','Réponse en fréquence du canal de propagation');
freqz(h,1,1024,16,'whole')
title('Réponse en fréquence du canal de propagation');
grid on;


%Paramètres
N=16;%Nombre de sous porteuses

N_bits=10000;%Nombre de bits
port_actives=16;%Porteurses actives
interPrefixe=2;%Prefixe cyclique

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

%DSP
[dsp,f] = pwelch(Ypref,[],[],[],N);

[dspSortieCanal,fbis] = pwelch(SignalSortieCanal,[],[],[],N);

figure('Name','DSP');
plot(f,10*log(dsp))
hold on
plot(fbis,10*log(dspSortieCanal))
grid
legend('DSPEntreeCanal','DSPSortieCanal')
xlabel('fréquence')
ylabel('dsp')

%Enlever l'intervalle de garde
SignalSortiebisGarde=reshape(SignalSortieCanal,size(Xepref));
SignalSortiebis=SignalSortiebisGarde(interPrefixe+1:end,:);

%Démodulation 

SignalSortieDemodule=fft(SignalSortiebis,N);

%ML
Ck=fft(h,N);
H=repmat(Ck(:),1,N_bits);
SignalSortieDemodEga=conj(H).*SignalSortieDemodule;

%Constellations
ConstPorteuse6=SignalSortieDemodEga(6,:);
ConstPorteuse15=SignalSortieDemodEga(15,:);

figure('Name','Constellations porteuses')
subplot(2,1,1)
scatter(real(ConstPorteuse6),imag(ConstPorteuse6))
title('Constellation porteuse 6')
xlabel('partie réel')
ylabel('partie imaginaire')
grid on
subplot(2,1,2)
scatter(real(ConstPorteuse15),imag(ConstPorteuse15))
title('Constellation porteuse 15')
grid on
xlabel('partie réel')
ylabel('partie imaginaire')

%TEB
Xrecepbin = SignalSortieDemodEga;
Xrecepbin(real(SignalSortieDemodEga)<0)=-1;
Xrecepbin(real(SignalSortieDemodEga)>0)=1;
TEB=mean(X~=Xrecepbin,"all");
fprintf('Le TEB simulé avec une égalisation ML est de %d\n',TEB)