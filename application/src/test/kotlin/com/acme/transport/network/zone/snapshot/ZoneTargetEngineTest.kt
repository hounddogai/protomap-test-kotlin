package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneTargetEngine()
        val item = newZoneTarget("code-218", "title-751")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
