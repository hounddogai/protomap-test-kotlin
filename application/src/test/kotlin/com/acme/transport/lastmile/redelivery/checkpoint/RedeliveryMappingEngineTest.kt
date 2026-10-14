package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryMappingEngine()
        val item = newRedeliveryMapping("code-907", "name-333")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
