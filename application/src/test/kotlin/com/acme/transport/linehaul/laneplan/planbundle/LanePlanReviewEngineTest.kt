package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanReviewEngine(newRegistry())
        val item = newLanePlanReview("code-77", "label-643")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
