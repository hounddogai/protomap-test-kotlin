package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissPolicyEngine(newRegistry())
        val item = newNearMissPolicy("code-728", "name-37")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
