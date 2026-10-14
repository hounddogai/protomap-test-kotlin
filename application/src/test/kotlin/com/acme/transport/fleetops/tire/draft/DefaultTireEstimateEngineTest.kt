package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTireEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTireEstimateEngine(newRegistry())
        val item = newDefaultTireEstimate("code-888", "name-965")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
