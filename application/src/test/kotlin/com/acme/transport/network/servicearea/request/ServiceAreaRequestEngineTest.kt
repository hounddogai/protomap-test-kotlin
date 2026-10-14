package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaRequestEngine()
        val item = newServiceAreaRequest("code-532", "label-852")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
