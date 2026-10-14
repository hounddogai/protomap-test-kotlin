package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualServicePlanSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualServicePlanSegmentEngine(newRegistry())
        val item = newManualServicePlanSegment("code-91", "title-318")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
