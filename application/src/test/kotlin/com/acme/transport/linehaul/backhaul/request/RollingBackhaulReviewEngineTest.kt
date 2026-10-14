package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBackhaulReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBackhaulReviewEngine(newRegistry())
        val item = newRollingBackhaulReview("code-35", "name-492")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
