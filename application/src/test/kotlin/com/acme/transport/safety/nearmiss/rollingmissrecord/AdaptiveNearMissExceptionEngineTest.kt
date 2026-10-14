package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNearMissExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNearMissExceptionEngine()
        val item = newAdaptiveNearMissException("code-210", "name-917")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
