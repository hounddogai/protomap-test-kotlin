package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeProfileEngine()
        val item = newTransitTimeProfile("code-790", "name-23")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
