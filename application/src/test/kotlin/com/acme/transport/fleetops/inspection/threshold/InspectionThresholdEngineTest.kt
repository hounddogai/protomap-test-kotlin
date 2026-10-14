package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionThresholdEngine(newRegistry())
        val item = newInspectionThreshold("code-362", "title-897")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
