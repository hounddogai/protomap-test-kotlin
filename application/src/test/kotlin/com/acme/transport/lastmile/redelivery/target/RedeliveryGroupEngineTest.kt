package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryGroupEngine()
        val item = newRedeliveryGroup("code-355", "name-175")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
