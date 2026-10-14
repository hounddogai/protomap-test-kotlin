package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingLanePlanSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingLanePlanSegmentEngine(newRegistry())
        val item = newPendingLanePlanSegment("code-717", "title-24")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
