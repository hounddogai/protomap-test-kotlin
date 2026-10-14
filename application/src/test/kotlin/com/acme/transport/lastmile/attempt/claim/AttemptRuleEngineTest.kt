package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptRuleEngine(newRegistry())
        val item = newAttemptRule("code-513", "label-407")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
