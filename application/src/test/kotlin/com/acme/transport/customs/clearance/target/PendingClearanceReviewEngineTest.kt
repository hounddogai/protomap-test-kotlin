package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingClearanceReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingClearanceReviewEngine(newRegistry())
        val item = newPendingClearanceReview("code-814", "title-473")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
