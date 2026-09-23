# Regras gerais

1. Erro no thymeleaf 
th:href="${/...
th:src="${/...
th:action="${/...

Esses provavelmente estão errados.

Essas expressões continuam como estão.
th:text="${platform.nmPlatform}"
th:if="${platform.stAtivo}"
th:object="${platform}"
th:field="*{nmPlatform}"

${...}  → dados, objetos, condições, valores
@{...}  → URLs
*{...}  → campos do objeto definido em th:object

2. Se possível formatar os arquivos .html para melhor visualização