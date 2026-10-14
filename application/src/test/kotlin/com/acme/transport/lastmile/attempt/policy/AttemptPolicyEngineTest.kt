package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptPolicyEngine(newRegistry())
        val item = newAttemptPolicy("code-835", "name-797")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
