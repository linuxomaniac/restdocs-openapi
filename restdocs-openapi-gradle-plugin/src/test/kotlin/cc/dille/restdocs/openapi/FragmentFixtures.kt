package cc.dille.restdocs.openapi


interface FragmentFixtures {

    fun rawFragment() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
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
                    examples:
                      example0: !include 'payment-integration-get-response.json'
        """.trimIndent()

    fun rawFragmentWithoutSchema() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
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
                    examples:
                      example0: !include 'payment-integration-get-response.json'
        """.trimIndent()

    fun rawFragmentWithEmptyResponse() = """
        /payment-integrations/{paymentIntegrationId}:
          get:
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
        """.trimIndent()

    fun rawFullFragment() = """
        /tags/{id}:
          put:
            parameters:
              - name: id
                in: path
                description: The id
                required: true
                schema:
                  type: integer
                example: 12
              - name: X-Custom-Header
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
                example: 42
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
                  examples:
                    example0: !include 'tags-create-request.json'
            responses:
              200:
                description: Update a tag
                headers:
                  X-Custom-Header:
                    description: A custom header
                    example: test
                content:
                  application/hal+json:
                    schema: !include 'tags-list-schema-response.json'
                    examples:
                      example0: !include 'tags-list-response.json'
        """.trimIndent()

    fun parsedFragmentMap(stringProvider: () -> String) = OpenAPIParser.parseFragment(stringProvider())
}
