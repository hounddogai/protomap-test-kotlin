package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTransitTimeWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTransitTimeWindowEngine(newRegistry())
        val item = newPrimaryTransitTimeWindow("code-790", "title-858")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
