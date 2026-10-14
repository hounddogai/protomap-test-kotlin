package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectScoreEngine(newRegistry())
        val item = newDefectScore("code-172", "label-430")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
