Ferramenta para gerar Module Ownership Matrices - MOM (Matrizes de Responsabilidade de Módulos)

Para execução, os seguintes parâmetros de entrada são necessários:

<identificador do repositório> <caminho local para onde o repositório foi clonado> <módulos>

Exemplo de execução com os parâmetros de entrada para o repositório Spring:

java -jar mom.jar spring /Users/joaopaulo/workspace/matrizes/spring-framework spring-beans spring-context spring-core spring-expression spring-jdbc spring-messaging spring-web

A ferramenta gera um arquivo .csv com a MOM no diretório /matrices/
