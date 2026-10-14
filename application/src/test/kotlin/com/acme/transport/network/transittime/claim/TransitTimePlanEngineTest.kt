package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimePlanEngine(newRegistry())
        val item = newTransitTimePlan("code-587", "title-560")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
