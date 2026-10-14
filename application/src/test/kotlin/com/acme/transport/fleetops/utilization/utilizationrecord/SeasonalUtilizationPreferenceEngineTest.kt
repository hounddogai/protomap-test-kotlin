package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalUtilizationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalUtilizationPreferenceEngine(newRegistry())
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
