# AdoPet

Aplicativo Android em Kotlin com SQLite no próprio celular. Versão 3.0-local.

## Para usar agora

O aplicativo atualizado já está instalado no emulador. Abra o ícone AdoPet.
Não é necessário abrir Iniciar-Adopet.bat, Python, MySQL ou qualquer servidor.

Para outro Android, instale AdoPet.apk do pacote. Requer Android 7.0 ou superior.
Para executar o código, abra a pasta android no Android Studio, aguarde a sincronização,
selecione um emulador ou celular e clique no triângulo Run. A primeira sincronização
pode precisar de internet para baixar as ferramentas. O aplicativo instalado funciona offline.

## Conta local de demonstração

E-mail: responsavel@adopet.local
Senha: Adopet@2026

Essa conta gerencia Luna e Nina, exemplos do Instituto Patinhas Felizes.
Mingau e Thor pertencem a outros responsáveis e ficam somente para consulta nessa conta.
Também é possível criar uma conta local como adotante ou responsável. Uma conta nova
de responsável pode cadastrar e administrar seus próprios animais.

## O que funciona

- Catálogo com fotos ilustrativas, filtros e detalhes.
- Login e cadastro local com dois perfis.
- Cadastro, edição e exclusão dos próprios animais.
- Tipo Outro com campo para escrever, como Coelho.
- Situação de adotado e registro de pré-adoção.
- Consulta dos interessados somente pelo responsável do animal.
- Validação dos campos e bloqueio de interesse duplicado por telefone e animal.
- Bloqueio de exclusão de animal com interesses, preservando o histórico.

## Estrutura simples

As Activities cuidam das telas. DatabaseHelper.kt usa SQLiteOpenHelper para criar,
consultar e atualizar adopet.db. Animal.kt representa um animal. PetImages.kt escolhe
uma fotografia ilustrativa por espécie. BaseActivity.kt ajusta as margens e executa
operações do banco em segundo plano, sem bloquear os botões.

O banco tem três tabelas: users, animals e interests. A sessão e os filtros usam
SharedPreferences. Não há API HTTP, biblioteca de rede, ORM ou servidor externo.
O SQL MySQL original foi referência para o domínio. Esta implementação simplifica
os responsáveis em users e guarda os dados do adotante em interests.

## Dados e limites

Cada instalação guarda suas próprias contas, animais e interesses. Não existe sincronização
entre celulares nem envio automático à ONG. As contas do MySQL anterior não são contas
desta versão. Limpar os dados ou desinstalar o aplicativo apaga os registros locais.

A atualização do banco SQLite antigo preserva animais e interesses, acrescentando os
campos novos. Animais antigos sem vínculo de conta continuam no catálogo, sem atribuição
automática a novos responsáveis. Somente os exemplos do Instituto ficam vinculados à
conta de demonstração. A versão anterior com MySQL permanece guardada separadamente.

Consulte TESTES.md, CREDITOS-IMAGENS.md e os materiais da pasta apresentacao do pacote.
