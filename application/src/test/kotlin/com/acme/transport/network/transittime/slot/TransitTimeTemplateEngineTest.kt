package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeTemplateEngine()
        val item = newTransitTimeTemplate("code-714", "name-993")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
