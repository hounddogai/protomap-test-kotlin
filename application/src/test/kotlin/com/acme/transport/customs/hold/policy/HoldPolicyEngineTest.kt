package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldPolicyEngine(newRegistry())
        val item = newHoldPolicy("code-98", "label-948")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
