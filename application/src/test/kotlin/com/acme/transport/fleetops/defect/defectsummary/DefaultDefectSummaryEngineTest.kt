package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDefectSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDefectSummaryEngine()
        val item = newDefaultDefectSummary("code-860", "label-927")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
