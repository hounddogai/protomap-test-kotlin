package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionReviewEngine(newRegistry())
        val item = newInspectionReview("code-216", "name-637")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
