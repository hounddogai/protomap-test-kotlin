package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneTierEngine()
        val item = newZoneTier("code-25", "label-784")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
