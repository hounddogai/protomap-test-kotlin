package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldMappingEngine()
        val item = newHoldMapping("code-936", "name-323")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
