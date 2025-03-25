clear all
close all

%réponse impulsionnelle
h=[0.407,0.815,0.407];
figure('Name','Réponse en fréquence du canal de propagation');
freqz(h,1,1024,16,'whole')
title('Réponse en fréquence du canal de propagation');
grid on;



%Constantes
N=16;%Nombre de sous porteuses
N_bits=10000;%Nombre de bits
port_actives=16;%Porteuses actives


%Mapping 
X = zeros(N,N_bits);
for i=1:port_actives
    bits=randi([0 1],1,N_bits);
    symboles=2*bits-1;
    X(i,:)=symboles';
end

%filtrage
Xe=ifft(X,N);
Y=reshape(Xe,1,[]);
SignalSortieCanal=filter(h,1,Y);

%DSP
[dsp,f] = pwelch(Y,[],[],[],N);

[dspSortieCanal,fbis] = pwelch(SignalSortieCanal,[],[],[],N);

figure('Name','DSP');
plot(f,10*log(dsp))
hold on
plot(fbis,10*log(dspSortieCanal))
grid
legend('DSPEntreeCanal','DSPSortieCanal')
xlabel('fréquence')
ylabel('dsp')

%Démodulation 
SignalSortiebis=reshape(SignalSortieCanal,size(Xe));
SignalSortieDemodule=fft(SignalSortiebis,N);

%Constellations
ConstPorteuse6=SignalSortieDemodule(6,:);
ConstPorteuse15=SignalSortieDemodule(15,:);

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
Xrecepbin = SignalSortieDemodule;
Xrecepbin(real(SignalSortieDemodule)<0)=-1;
Xrecepbin(real(SignalSortieDemodule)>0)=1;

TEB=mean(X~=Xrecepbin,"all");
fprintf('Le TEB simulé sans garde est de %d\n',TEB)



