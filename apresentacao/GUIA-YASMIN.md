# Yasmin: funcionalidades e persistência do AdoPet

## A explicação principal

“O AdoPet usa Kotlin nas telas e SQLite para guardar os dados no próprio Android.
Os botões chamam funções que validam os campos, verificam a conta e fazem as
operações de cadastro, consulta, edição e exclusão. O banco mantém os dados
mesmo depois de fechar o app. Esta versão é uma demonstração local.”

Não existe um backend em servidor nesta versão. Sua parte corresponde às
funcionalidades, regras e persistência local. Isso é importante para responder
com precisão, sem chamar SQLite de API ou dizer que usamos Python.

## Do primeiro arquivo até a versão final

O primeiro ZIP tinha telas, identidade visual e um catálogo estático. Os botões
não completavam todas as operações. O arquivo BancoAdoePet.sql descrevia um banco
MySQL externo com responsáveis, animais, adotantes e interesses. Ele sozinho
não conectava o aplicativo ao banco.

Na versão atual, as telas operam sobre registros reais no SQLite. O modelo foi
simplificado: users reúne as contas, animals guarda os animais, interests guarda
o formulário do adotante e o vínculo com o animal. Não são as mesmas quatro
tabelas do arquivo original. Houve uma versão com API e MySQL, preservada à parte.
A escolha final é o banco local para reduzir a configuração e facilitar o ensaio.

Mantivemos o logotipo e a organização do catálogo em duas colunas. Acrescentamos
fotografias ilustrativas, navegação, filtros, formulários, validações, contas locais
e permissões. As fotografias não vieram no primeiro ZIP. Seus créditos estão no pacote.

## Por que SQLite

É um banco que o Android já oferece. Não precisamos instalar um servidor no computador.
O app funciona offline. Os registros ficam em um arquivo privado chamado adopet.db.
Um banco é útil porque organiza os dados em tabelas e permite buscar, alterar e relacionar
registros. Uma lista só na memória desapareceria ao encerrar o processo.

A consequência da escolha é clara: cada aparelho tem seu banco. Não dá para anunciar
em um celular e esperar que apareça em outro. Para uma rede real de adoção, precisaríamos
planejar armazenamento compartilhado, normalmente com um servidor e uma API.

## O que acontece ao salvar um animal

1. AnimalFormActivity lê nome, idade, espécie, porte e os outros campos.
2. A tela verifica campos vazios e idade. Se escolher Outro, exige o tipo por escrito.
3. Ela monta um objeto Animal. Esse objeto transporta os dados, não salva sozinho.
4. insertAnimal() no DatabaseHelper verifica os dados e se a conta é responsável.
5. O código usa o ID da conta conectada como owner_id. Não aceita escolher outro dono.
6. ContentValues reúne os valores de cada coluna e insertOrThrow grava a linha.
7. O SQLite devolve o ID novo. A tela informa sucesso e volta ao catálogo.
8. onResume() recarrega a lista, que passa a mostrar o registro.

Exemplo para falar: “Ao clicar Salvar, validamos o formulário e gravamos uma linha
na tabela animals. Quando a tela principal reaparece, ela consulta o banco outra vez.”

## CRUD, palavra que você precisa saber

| Operação | No projeto | O que faz |
| --- | --- | --- |
| Create | insertAnimal | Cria um animal |
| Read | getAnimals / getAnimal | Consulta lista ou detalhes |
| Update | updateAnimal | Altera um animal existente pelo ID |
| Delete | deleteAnimal | Exclui um animal após verificar as regras |

O ID identifica uma linha. Editar altera a linha com aquele ID. Não cria outro
animal com o mesmo nome. Nome pode repetir, ID não.

## Tabelas e relacionamentos

users: id, nome, e-mail, telefone, hash da senha, salt e perfil.
animals: id, dados do animal, situação e owner_id.
interests: id, animal_id, nome e contatos do interessado, moradia, telas,
experiência, motivo, data, e-mail e confirmação de responsabilidade.

owner_id referencia users.id. Um responsável pode ter vários animais.
animal_id referencia animals.id. Um animal pode receber vários interesses.
Isso é uma relação de um para muitos. A chave primária identifica a linha.
A chave estrangeira mantém o vínculo e impede referência a um registro inexistente.

Neste escopo, os dados de contato ficam na própria linha do interesse. Não há
uma tabela separada de adotantes. É uma simplificação para o formulário local.

SQLiteOpenHelper chama onCreate() quando o banco ainda não existe e onUpgrade()
quando a versão do esquema muda. A versão atual é 3. A atualização acrescenta
contas e colunas ao banco antigo, preservando animais e interesses existentes.
Os quatro animais iniciais são dados fictícios para a demonstração.

## Como os filtros funcionam

FilterActivity guarda as opções no SharedPreferences. Ao voltar, MainActivity
pede ao DatabaseHelper os animais que atendem às condições. Ele monta uma cláusula
WHERE com AND. “Outro” procura espécies diferentes de Gato e Cachorro. “Somente
disponíveis” acrescenta adopted=0. Os valores vão em parâmetros ?, sem concatenar
o que a pessoa digitou dentro do SQL.

SharedPreferences serve para configurações pequenas, como filtros e sessão.
Os cadastros ficam no SQLite. São ferramentas diferentes para dados diferentes.

## Login e permissão

Autenticação é verificar a conta e a senha. Autorização é conferir o que aquela
conta pode fazer. Ao cadastrar, escolhemos adotante ou responsável. O login busca
o e-mail e compara o resultado da função de senha. A sessão guarda ID, nome e perfil
no SharedPreferences local. Sair apaga essa sessão, sem apagar cadastros.

Não guardamos a senha em texto simples. Usamos a função pronta PBKDF2 com salt aleatório.
Salt é um valor diferente por conta, usado junto da senha no cálculo. Hash não é
criptografia reversível. Não inventamos um algoritmo de proteção. É uma parte que
usa recursos prontos da plataforma, mesmo em um projeto simples.

Visitante/adotante consulta e registra pré-adoção. Responsável cadastra animais.
Antes de editar, excluir ou consultar contatos, requireOwner() verifica se o owner_id
é o ID da conta conectada. Esconder botões facilita a interface, mas o DatabaseHelper
também verifica a regra antes da operação. Criar uma conta com o mesmo nome de uma
ONG não dá acesso aos animais dela, pois o vínculo usa ID, não apenas nome.

É um controle local de protótipo. Não é um sistema de autenticação online nem verificação
da identidade de uma ONG. Não há recuperação de senha por e-mail.

## Pré-adoção e regras

O formulário exige nome, contato, moradia, motivo, e-mail válido e confirmação de
responsabilidade. O telefone fica só com dígitos e precisa ter de 10 a 13 dígitos.
Antes de gravar, conferimos disponibilidade e se já existe interesse com o mesmo
telefone naquele animal. Isso impede que o mesmo telefone gere pedidos repetidos.

Uma transação reúne verificação e inserção: só confirma a operação se tudo der certo.
O aplicativo não registra interesse em animal adotado. Não exclui animal que tem
interesses, para preservar o histórico. Marcar adotado muda a situação do registro.
Enviar interesse não conclui a adoção e não manda mensagem à ONG. O responsável
consulta os contatos naquela mesma instalação e o contato posterior acontece por fora.

## Como ler os arquivos

| Arquivo Kotlin | O que procurar |
| --- | --- |
| MainActivity | Catálogo, botões de conta e carregamento da lista |
| FilterActivity | Opções e preferências dos filtros |
| AnimalDetailActivity | Detalhes, botões permitidos e confirmação de exclusão |
| AnimalFormActivity | Leitura do formulário, Outro, cadastro e edição |
| InterestActivity | Validação e gravação da pré-adoção |
| LoginActivity / RegisterActivity | Entrada e criação de conta local |
| DatabaseHelper | Esquema, consultas, regras, contas e persistência |
| Animal | Campos que representam um animal |
| BaseActivity | Margens e operações fora da thread da tela |
| PetImages | Escolha da imagem ilustrativa por espécie |

As interfaces ficam em XML em res/layout. Activities respondem aos cliques em Kotlin.
Intent abre outra tela e leva o animal_id. Não transportamos a lista inteira para
a tela de detalhes, consultamos o animal escolhido pelo ID.

O banco roda em uma Thread para não bloquear a interface. updateScreen usa a thread
principal para mudar os componentes visuais. Se ocorrer um erro, mostramos uma mensagem.
A tela não deve anunciar “salvo” quando a gravação falha.

## Perguntas prováveis

“Por que não MySQL?” Porque escolhemos uma demonstração local offline, e SQLite já
faz parte do Android. MySQL precisaria de outra configuração e de uma conexão adequada.

“Tem API?” Não há API HTTP nem backend em servidor na versão final. Usamos as classes
do Android para acessar o SQLite dentro do próprio aplicativo.

“Onde está sua parte?” Nas funções de cadastro, consulta, edição, exclusão, filtros,
regras de acesso, validações e gravação local. Explique o que cada função faz e demonstre.

“Qualquer pessoa pode excluir?” A interface e o DatabaseHelper exigem conta de
responsável e vínculo com aquele animal. Com interesse registrado, a exclusão é bloqueada.

“Se fechar o aplicativo perde tudo?” Não. O arquivo do SQLite permanece. Limpar dados
ou desinstalar apaga o banco desta instalação. Não existe cópia na nuvem.

“O SQL original é o banco atual?” Não. Foi referência do domínio. A versão local tem
três tabelas e adaptações de SQLite, criadas pelo DatabaseHelper.

“Os testes comprovam que tudo está perfeito?” Não. Eles verificam os casos descritos
em TESTES.md. Ainda devemos ensaiar e executar a avaliação por outro grupo.

## Treino antes de apresentar

1. Abra a tela do catálogo e explique de onde vêm os registros.
2. Entre como responsável e cadastre um Coelho com Outro.
3. Mostre o trecho insertAnimal e relacione cada campo à coluna correspondente.
4. Edite o nome e feche/reabra o app. Explique por que a alteração continua salva.
5. Exclua esse registro antes de criar interesse nele.
6. Saia da conta e explique por que os botões de gestão desapareceram.
7. Registre interesse em Luna com dados fictícios. Entre e consulte o resultado.
8. Tente repetir o telefone e explique o bloqueio.
9. Explique a diferença entre conta, perfil, ID e chave estrangeira com seus próprios exemplos.

Aprenda os caminhos acima e pratique com o código aberto. Não precisa decorar todos
os comandos para conseguir explicar o raciocínio e reconhecer onde cada regra está.
