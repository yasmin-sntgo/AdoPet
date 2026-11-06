# Requisitos do AdoPet

## Público-alvo

Pessoas interessadas em adotar animais de estimação e ONGs ou protetores responsáveis por divulgar os animais.

## Necessidades dos usuários

- localizar animais adequados ao seu estilo de vida;
- consultar informações de saúde, personalidade e localização;
- demonstrar interesse em uma adoção de forma simples;
- cadastrar e manter atualizadas as informações dos animais.

## Requisitos funcionais

- RF01: apresentar os animais cadastrados em uma lista;
- RF02: permitir filtrar a lista por espécie, porte, sexo e disponibilidade;
- RF03: apresentar os detalhes de um animal selecionado;
- RF04: permitir cadastrar um animal;
- RF05: permitir editar um animal;
- RF06: permitir excluir um animal após confirmação;
- RF07: permitir marcar um animal como adotado;
- RF08: permitir registrar interesse em adoção;
- RF09: validar os campos obrigatórios dos formulários;
- RF10: armazenar animais, contas e interesses em SQLite local.

## Critérios de aceitação

- um animal salvo deve aparecer no catálogo;
- uma alteração deve aparecer no catálogo e nos detalhes;
- um animal excluído não deve continuar na lista;
- os filtros devem apresentar somente resultados correspondentes;
- um animal adotado deve exibir essa situação e não aceitar novo interesse;
- formulários incompletos não devem ser salvos;
- os dados devem permanecer após fechar e abrir o aplicativo.

## Controle de acesso

- Visitante e adotante consultam e registram interesse.
- Responsável autenticado cadastra e gerencia somente os próprios animais.
- Somente o responsável do animal consulta os contatos interessados.
- O DatabaseHelper verifica o vínculo do responsável antes de alterar dados ou consultar contatos.
- A pré-adoção exige e-mail válido e confirmação de responsabilidade.

## Limite do escopo

As contas e os dados pertencem a esta instalação, sem sincronização entre aparelhos.
