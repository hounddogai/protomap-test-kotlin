package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeBundleEngine()
        val item = newTransitTimeBundle("code-669", "name-550")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
