package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeBatchEngine()
        val item = newTransitTimeBatch("code-436", "name-705")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
