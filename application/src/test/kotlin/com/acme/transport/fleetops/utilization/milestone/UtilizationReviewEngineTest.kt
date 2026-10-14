package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationReviewEngine(newRegistry())
        val item = newUtilizationReview("code-2", "name-65")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
