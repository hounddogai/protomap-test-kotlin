package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServiceAreaVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServiceAreaVarianceEngine()
        val item = newCappedServiceAreaVariance("code-560", "label-899")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
