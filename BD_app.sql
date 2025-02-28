create database AppUber;
use AppUber;

create table Cadastro(
cadastroID int primary key,
nomeUsuario varchar(200) not null,
cpfUsuario int unique not null,
emailUsuario varchar(150) unique not null,
telefoneUsuario int not null,
senhaUsuario varchar(100) not null
);

create table Login(
emailUsuario varchar(150) unique not null,
senhaUsuario varchar(100) not null,
loginTimestamp datetime default current_timestamp,
foreign key (emailUsuario) references Cadastro(emailUsuario)
);
