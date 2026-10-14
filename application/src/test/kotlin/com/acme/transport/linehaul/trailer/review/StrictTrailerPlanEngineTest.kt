package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTrailerPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTrailerPlanEngine(newRegistry())
        val item = newStrictTrailerPlan("code-8", "label-865")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
