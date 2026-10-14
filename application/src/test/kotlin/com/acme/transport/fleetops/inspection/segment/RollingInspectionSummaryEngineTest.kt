package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingInspectionSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingInspectionSummaryEngine(newRegistry())
        val item = newRollingInspectionSummary("code-227", "title-948")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
