package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDeclarationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDeclarationBatchEngine()
        val item = newManualDeclarationBatch("code-520", "name-708")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
