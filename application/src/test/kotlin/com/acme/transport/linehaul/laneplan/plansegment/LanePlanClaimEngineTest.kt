package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanClaimEngine(newRegistry())
        val item = newLanePlanClaim("code-355", "title-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
