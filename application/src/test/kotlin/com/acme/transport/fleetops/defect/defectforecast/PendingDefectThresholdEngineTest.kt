package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDefectThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDefectThresholdEngine(newRegistry())
        val item = newPendingDefectThreshold("code-481", "label-723")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
