package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationThresholdEngine(newRegistry())
        val item = newUtilizationThreshold("code-700", "name-757")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
