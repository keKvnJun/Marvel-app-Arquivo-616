# Arquivo 616

Aplicativo Android escolar em Java que transforma dados da Comic Vine em dossiês visuais inspirados em quadrinhos e na interface da S.H.I.E.L.D.

## Destaques

* catálogo e pesquisa inteligente de personagens Marvel, com aliases em português
* detalhes com poderes, equipes e aparições
* coleção especial com 20 sementes do Spider Verso
* favoritos, histórico e temas claro e escuro
* pacotes de cartas, coleção persistente, deck de até três cartas e partidas rápidas
* JARVIS limitado ao universo Marvel
* reconhecimento visual opcional com Gemini
* carta comemorativa do Stan Lee
* abertura cinematográfica, transições em estilo HQ e identidade própria para o Spider Verso

## Configuração

Use Android Studio com JDK 21. No arquivo local `local.properties`, preserve `sdk.dir` e adicione:

```properties
COMIC_VINE_API_KEY=sua_chave
GEMINI_API_KEY=sua_chave
```

A chave da Comic Vine habilita o catálogo real. A chave Gemini é opcional e habilita a análise de imagens capturadas pela câmera. Nunca envie `local.properties` ao Git.

## Arquitetura

O projeto utiliza Java, XML, Fragments, Navigation Component, ViewModel, LiveData, Retrofit, Gson, Glide, RecyclerView e SharedPreferences. As listas solicitam apenas campos leves e os detalhes são carregados sob demanda.

## Execução

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Adicione as chaves ao `local.properties`.
4. Execute em um aparelho ou emulador com Android 8 ou superior.

## Fontes e uso

Dados de personagens são fornecidos pela [Comic Vine](https://comicvine.gamespot.com/api/). As métricas do duelo são mecânicas próprias baseadas em contagens editoriais e não representam níveis oficiais de poder.

As colagens de quadrinhos incluídas foram fornecidas para demonstração escolar. Antes de qualquer distribuição pública ou comercial, os direitos desses recursos devem ser revisados. Os efeitos de splash, transição e portal foram implementados com XML, vetores e Canvas, sem incorporar o vídeo usado como referência.

## Regras das cartas

Cada pacote contém três cartas, prioriza personagens ainda não descobertos e usa probabilidades diferentes para cada raridade. A coleção e o deck ficam salvos no aparelho com `SharedPreferences`. As partidas usam três rodadas, o jogador escolhe o atributo e o adversário é sorteado localmente, sem depender de rede ou de inteligência artificial.

Os valores são índices lúdicos criados para o Arquivo 616. Eles não devem ser apresentados como estatísticas oficiais da Marvel.
