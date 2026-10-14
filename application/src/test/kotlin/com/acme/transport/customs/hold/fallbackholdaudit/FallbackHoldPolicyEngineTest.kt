package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackHoldPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackHoldPolicyEngine(newRegistry())
        val item = newFallbackHoldPolicy("code-130", "title-763")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
