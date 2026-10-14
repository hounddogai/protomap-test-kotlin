package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationAdjustmentEngine(newRegistry())
        val item = newUtilizationAdjustment("code-680", "name-786")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
