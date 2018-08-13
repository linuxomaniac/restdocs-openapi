package cc.dille.restdocs.openapi.plugin.common



interface FragmentFixtures {

    fun rawFragment() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            summary: get-payment
            parameters:
              - name: paymentIntegrationId
                in: path
                description: The id
                required: true
                schema:
                  type: integer
                example: 12
            responses:
              200:
                description: some
                content:
                  application/hal+json:
                    schema: !include 'payment-integration-get-schema-response.json'
                    example: !include 'payment-integration-get-response.json'
        """.trimIndent()

    fun rawFragmentWithoutSchema() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            summary: get-payment
            parameters:
              - name: paymentIntegrationId
                in: path
                description: The id
                required: true
                schema:
                  type: integer
                example: 12
            responses:
              200:
                description: some description
                content:
                  application/hal+json:
                    example: !include 'payment-integration-get-response.json'
        """.trimIndent()

    fun rawFragmentWithEmptyResponse() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
            summary: get-payment
            parameters:
              - name: paymentIntegrationId
                in: path
                description: The id
                required: true
                schema:
                  type: integer
                example: 12
            responses:
              200:
                headers:
                  Test-ResponseHeader:
                    description: hello
                description: some
        """.trimIndent()

    fun rawFullFragment() = """
        /tags/{id}:
          put:
            summary: put-tag
            operationId: putTag
            parameters:
              - name: id
                in: path
                description: The id
                required: true
                schema:
                  type: integer
                example: 12
              - name: X-Custom-ResponseHeader
                in: header
                description: A custom header
                required: true
                schema:
                  type: string
                example: test
              - name: some
                in: query
                description: some
                required: false
                schema:
                  type: integer
              - name: other
                in: query
                description: other
                required: true
                schema:
                  type: string
                example: test
            requestBody:
              required: true
              content:
                application/hal+json:
                  schema: !include 'tags-create-schema-request.json'
                  example: !include 'tags-create-request.json'
            responses:
              200:
                description: Update a tag
                headers:
                  X-Custom-ResponseHeader:
                    description: A custom header
                    example: test
                content:
                  application/hal+json:
                    schema: !include 'tags-list-schema-response.json'
                    example: !include 'tags-list-response.json'
        """.trimIndent()

    fun parsedFragmentMap(stringProvider: () -> String) = OpenAPIParser.parseFragment(stringProvider())
}
