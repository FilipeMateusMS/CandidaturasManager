# Regras gerais

1. Erro no Thymeleaf

As expressões Thymeleaf devem ser escritas diretamente, sem barra invertida antes de $.

Correto:
```html
th:text="${platform.nmPlatform}"
th:if="${platform.stAtivo}"
th:object="${platform}"
th:field="*{nmPlatform}"
```

Errado:
```html
th:text="\${platform.nmPlatform}"
th:if="\${platform.stAtivo}"
```

Nunca usar `\${...}` dentro dos arquivos `.html`. A barra invertida pode aparecer somente como escape na geração do arquivo por uma ferramenta, mas não pode existir no HTML final.

2. Sintaxe Thymeleaf

`${...}` → dados, objetos, condições e valores.
`@{...}` → URLs geradas pelo Thymeleaf para rotas da aplicação.
`*{...}` → campos do objeto definido em `th:object`.

Exemplos:
```html
th:text="${platform.nmPlatform}"
th:if="${platform.stAtivo}"
th:href="@{/editar/{id}(id=${platform.cdPlatform})}"
th:object="${platform}"
th:field="*{nmPlatform}"
```

3. URL externa armazenada no banco

Quando a URL já está armazenada em uma propriedade do objeto e deve ser usada diretamente como endereço externo, não envolver a expressão com `@{...}`.

Correto:
```html
th:href="${plataforma.dsUrl}"
```

Evitar:
```html
th:href="@{${plataforma.dsUrl}}"
```

4. URLs internas

Para URLs que pertencem às rotas da própria aplicação, usar `@{...}`.

Exemplo:
```html
th:href="@{/}"
th:href="@{/editar/{id}(id=${plataforma.cdPlatform})}"
th:action="@{/toggle/{id}(id=${plataforma.cdPlatform})}"
```

5. Formatação HTML

Se possível, formatar os arquivos `.html` para melhor visualização e manutenção.