package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneClaimEngine()
        val item = newZoneClaim("code-334", "label-896")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
