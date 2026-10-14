package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationProfileEngine(newRegistry())
        val item = newUtilizationProfile("code-839", "label-222")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
