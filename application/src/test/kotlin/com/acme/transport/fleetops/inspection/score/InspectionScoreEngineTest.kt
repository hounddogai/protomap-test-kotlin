package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionScoreEngine(newRegistry())
        val item = newInspectionScore("code-711", "label-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
