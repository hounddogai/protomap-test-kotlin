package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualEvChargeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualEvChargeTierEngine()
        val item = newManualEvChargeTier("code-831", "title-41")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
