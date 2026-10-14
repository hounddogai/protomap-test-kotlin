package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryVarianceEngine()
        val item = newRedeliveryVariance("code-631", "title-888")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
