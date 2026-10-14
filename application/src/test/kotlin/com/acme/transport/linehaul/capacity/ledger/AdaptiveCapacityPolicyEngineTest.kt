package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCapacityPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCapacityPolicyEngine(newRegistry())
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
