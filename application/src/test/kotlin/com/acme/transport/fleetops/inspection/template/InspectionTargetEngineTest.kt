package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionTargetEngine(newRegistry())
        val item = newInspectionTarget("code-791", "title-435")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
