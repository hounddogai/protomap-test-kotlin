package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanProposalEngine(newRegistry())
        val item = newLanePlanProposal("code-717", "title-867")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
