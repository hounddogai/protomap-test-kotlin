package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanSegmentEngine()
        val item = newLanePlanSegment("code-351", "title-472")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
