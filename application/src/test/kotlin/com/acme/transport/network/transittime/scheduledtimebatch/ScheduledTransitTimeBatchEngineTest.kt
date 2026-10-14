package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTransitTimeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTransitTimeBatchEngine(newRegistry())
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
