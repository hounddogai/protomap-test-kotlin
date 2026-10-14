package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationDigestEngine()
        val item = newUtilizationDigest("code-505", "title-503")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
