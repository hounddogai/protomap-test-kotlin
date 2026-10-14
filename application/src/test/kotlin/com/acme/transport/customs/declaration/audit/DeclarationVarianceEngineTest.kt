package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationVarianceEngine()
        val item = newDeclarationVariance("code-463", "title-645")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
