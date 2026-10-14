package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeEntryEngine()
        val item = newTransitTimeEntry("code-473", "title-654")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
