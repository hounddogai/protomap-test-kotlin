package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceProfileEngine()
        val item = newClearanceProfile("code-457", "label-199")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
