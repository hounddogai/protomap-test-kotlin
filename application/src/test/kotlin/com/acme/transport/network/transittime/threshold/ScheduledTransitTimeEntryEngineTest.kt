package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTransitTimeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTransitTimeEntryEngine(newRegistry())
        val item = newScheduledTransitTimeEntry("code-239", "label-250")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
