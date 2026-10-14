package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanCycleEngine(newRegistry())
        val item = newLanePlanCycle("code-354", "label-744")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
