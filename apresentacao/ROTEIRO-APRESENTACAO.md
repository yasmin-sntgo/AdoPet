# Apresentação do AdoPet

Livia Beatriz, Vinicius Eduardo e Yasmin Santiago.
Sugestão: 8 a 10 minutos no total, incluindo 2 a 3 minutos de demonstração.

## Distribuição de fala sugerida

Livia apresenta a ideia e o processo nos slides 1 a 3, em cerca de 1min30.
Vinicius apresenta as funcionalidades no slide 4 e testes/limites no slide 9,
em cerca de 1min. Yasmin explica slides 5 a 8, em cerca de 3min.
No slide 10, o grupo demonstra o aplicativo. Essa distribuição é uma sugestão
de apresentação, não uma afirmação de quem escreveu cada arquivo.

## Fala central da Yasmin

“Usamos Kotlin nas telas e SQLite no próprio Android. Os formulários chamam funções
do DatabaseHelper, que validam os dados e fazem cadastro, consulta, edição e exclusão.
O banco tem contas, animais e interesses. O ID conecta cada animal ao responsável
e cada interesse ao animal. Conferimos esse vínculo para permitir a edição somente
ao responsável. Escolhemos o armazenamento local para funcionar offline e simplificar
a execução. Cada aparelho guarda seus próprios registros.”

## Antes de começar

Abra os slides e deixe o emulador com o AdoPet aberto. Não precisa iniciar servidor.
Use somente os materiais da versão local. Confira a conta de demonstração e ensaie
o roteiro inteiro uma vez. Selecione sem filtros no início. Não limpe os dados do app.
Tenha o APK disponível caso precise executar em outro Android, mas prefira testar
o aparelho com antecedência. O PDF é uma cópia dos slides para eventual falha do PowerPoint.

Conta local: responsavel@adopet.local, senha Adopet@2026. Ela gerencia Luna e Nina.

## Demonstração de 2 a 3 minutos

1. Como visitante, mostre catálogo, filtro Gato e detalhes. Limpe o filtro.
2. Entre com a conta de demonstração e abra Cadastrar pet.
3. Escolha Outro e escreva Coelho. Complete o cadastro com dados fictícios.
4. Mostre no catálogo, edite o nome e exclua esse registro temporário.
5. Saia. Registre pré-adoção em Luna usando telefone fictício diferente do ensaio.
6. Entre novamente e consulte Interesses registrados no perfil de Luna.
7. Feche e reabra o app para mostrar que os dados permanecem.

Excluir antes de registrar interesse no animal temporário evita o bloqueio de
preservação de histórico. Não exclua os exemplos durante a apresentação.

## O que explicar sem prometer além da versão

Fotos são ilustrativas. Interesse não finaliza adoção e não manda mensagem à ONG.
Contas e dados são locais. Não há sincronização, hospedagem nem recuperação de senha.
O arquivo SQL MySQL original foi referência, mas o banco atual é SQLite simplificado.
O teste por outro grupo ainda precisa acontecer, conforme a atividade da turma.

## Se algo não aparecer

Limpe os filtros e use Atualizar. Confira se entrou como responsável e se o animal
é da conta. Mingau e Thor são exemplos de outros responsáveis. Telefone já usado
no mesmo animal gera bloqueio de duplicidade. Não use a versão antiga do APK com MySQL.
