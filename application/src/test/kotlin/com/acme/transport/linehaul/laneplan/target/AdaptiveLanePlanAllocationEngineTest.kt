package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLanePlanAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLanePlanAllocationEngine(newRegistry())
        val item = newAdaptiveLanePlanAllocation("code-25", "title-757")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
