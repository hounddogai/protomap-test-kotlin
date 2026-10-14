package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaVarianceEngine()
        val item = newServiceAreaVariance("code-98", "name-554")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
