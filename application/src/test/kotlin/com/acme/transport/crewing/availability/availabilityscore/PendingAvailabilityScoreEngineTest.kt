package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAvailabilityScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAvailabilityScoreEngine(newRegistry())
        val item = newPendingAvailabilityScore("code-496", "title-717")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
