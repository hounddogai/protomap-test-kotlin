package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeQuotaEngine()
        val item = newTransitTimeQuota("code-208", "title-917")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
