package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectMilestoneEngine(newRegistry())
        val item = newDefectMilestone("code-153", "label-501")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
