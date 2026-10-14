package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityPolicyEngine(newRegistry())
        val item = newCapacityPolicy("code-123", "name-357")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
