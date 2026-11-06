# Verificação da versão local

O APK e o APK de testes compilaram no Android Studio e foram instalados no emulador.
LocalInstrumentation executou testes em bancos separados, sem alterar os cadastros
da apresentação. Resultado: todos passaram.

Casos executados: criação de banco com quatro exemplos, CRUD, filtro Outro,
persistência após fechar e reabrir a conexão, senha incorreta, login correto,
cadastro e e-mail duplicado, visitante e adotante sem permissão de gestão,
outro responsável sem acesso à edição, exclusão e contatos de animal alheio,
validação de idade, e-mail e consentimento, telefone normalizado e duplicidade,
animal adotado, preservação de histórico e PRAGMA foreign_key_check.

Uma base de teste na versão 2 foi atualizada para a versão 3 sem perder o animal
nem o interesse. A conta de demonstração recebeu apenas o exemplo do Instituto.

O app real também foi aberto no emulador com o catálogo e as fotografias.
O manifest não solicita acesso à internet. Os testes não fazem chamadas de rede.

Isso não substitui ensaio da apresentação e teste por outro grupo.
TESTES-OUTRO-GRUPO.md é um formulário em branco para essa avaliação.

Para repetir: execute a configuração de instrumentação LocalInstrumentation no
Android Studio ou instale os dois APKs gerados e use:

    adb shell am instrument -w com.adopet.app.test/com.adopet.app.test.LocalInstrumentation
