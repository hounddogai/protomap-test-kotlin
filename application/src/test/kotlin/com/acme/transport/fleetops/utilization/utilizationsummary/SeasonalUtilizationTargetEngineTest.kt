package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalUtilizationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalUtilizationTargetEngine(newRegistry())
        val item = newSeasonalUtilizationTarget("code-404", "label-12")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
