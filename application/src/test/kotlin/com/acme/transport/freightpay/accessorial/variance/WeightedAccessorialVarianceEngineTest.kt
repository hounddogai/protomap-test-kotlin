package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAccessorialVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAccessorialVarianceEngine()
        val item = newWeightedAccessorialVariance("code-774", "title-736")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
