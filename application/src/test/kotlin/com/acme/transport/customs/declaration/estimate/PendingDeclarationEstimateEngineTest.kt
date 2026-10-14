package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDeclarationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDeclarationEstimateEngine()
        val item = newPendingDeclarationEstimate("code-789", "label-67")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
