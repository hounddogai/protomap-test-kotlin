package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingExceptionEngine()
        val item = newIdlingException("code-610", "label-491")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
