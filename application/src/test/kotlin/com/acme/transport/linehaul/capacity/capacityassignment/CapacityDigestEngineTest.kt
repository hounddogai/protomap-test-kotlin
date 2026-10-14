package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityDigestEngine()
        val item = newCapacityDigest("code-431", "title-776")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
