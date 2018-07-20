package cc.dille.restdocs.openapi

import java.io.File

object NoOpJsonSchemaMerger: JsonSchemaMerger(File("none.json")) {
        override fun mergeSchemas(schemas: List<Include>): Include = schemas.first()
    }
