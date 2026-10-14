package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryPolicyEngine()
        val item = newRedeliveryPolicy("code-632", "title-166")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
