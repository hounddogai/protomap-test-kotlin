package com.acme.transport.linehaul.capacity.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityQuotaEngine()
        val item = newCapacityQuota("code-125", "title-313")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
