package com.acme.transport.network.zone.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneWindowEngine()
        val item = newZoneWindow("code-638", "title-784")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
