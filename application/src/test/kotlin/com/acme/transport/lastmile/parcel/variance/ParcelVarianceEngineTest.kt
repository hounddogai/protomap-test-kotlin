package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelVarianceEngine()
        val item = newParcelVariance("code-237", "name-496")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
