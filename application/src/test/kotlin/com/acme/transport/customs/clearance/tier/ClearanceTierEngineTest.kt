package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceTierEngine()
        val item = newClearanceTier("code-544", "label-106")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
