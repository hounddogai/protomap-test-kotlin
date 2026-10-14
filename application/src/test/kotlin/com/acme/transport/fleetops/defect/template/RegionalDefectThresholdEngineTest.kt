package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDefectThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDefectThresholdEngine(newRegistry())
        val item = newRegionalDefectThreshold("code-976", "title-955")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
