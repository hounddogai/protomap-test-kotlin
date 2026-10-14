package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDeclarationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDeclarationManifestEngine()
        val item = newBatchedDeclarationManifest("code-66", "name-819")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
