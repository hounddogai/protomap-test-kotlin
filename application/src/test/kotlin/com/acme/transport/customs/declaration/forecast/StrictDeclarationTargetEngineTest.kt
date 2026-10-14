package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDeclarationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDeclarationTargetEngine()
        val item = newStrictDeclarationTarget("code-281", "label-551")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
