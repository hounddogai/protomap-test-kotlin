package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalUtilizationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalUtilizationSegmentEngine(newRegistry())
        val item = newRegionalUtilizationSegment("code-102", "label-679")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
