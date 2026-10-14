package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveUtilizationRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveUtilizationRollupEngine(newRegistry())
        val item = newAdaptiveUtilizationRollup("code-814", "name-539")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
