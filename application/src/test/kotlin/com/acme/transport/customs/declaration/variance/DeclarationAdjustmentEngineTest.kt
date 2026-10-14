package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationAdjustmentEngine()
        val item = newDeclarationAdjustment("code-561", "label-734")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
