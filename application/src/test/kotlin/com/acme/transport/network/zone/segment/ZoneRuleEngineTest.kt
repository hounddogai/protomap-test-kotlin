package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneRuleEngine()
        val item = newZoneRule("code-970", "title-449")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
