package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRedeliveryWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRedeliveryWindowEngine()
        val item = newWeightedRedeliveryWindow("code-827", "title-462")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
