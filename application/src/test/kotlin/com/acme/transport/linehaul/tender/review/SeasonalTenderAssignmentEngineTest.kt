package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTenderAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTenderAssignmentEngine(newRegistry())
        val item = newSeasonalTenderAssignment("code-984", "label-263")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
