package com.acme.transport.customs.declaration.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationThresholdEngine()
        val item = newDeclarationThreshold("code-526", "title-740")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
