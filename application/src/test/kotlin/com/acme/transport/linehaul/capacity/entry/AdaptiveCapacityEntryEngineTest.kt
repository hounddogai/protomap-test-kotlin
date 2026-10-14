package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCapacityEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCapacityEntryEngine()
        val item = newAdaptiveCapacityEntry("code-331", "title-727")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
