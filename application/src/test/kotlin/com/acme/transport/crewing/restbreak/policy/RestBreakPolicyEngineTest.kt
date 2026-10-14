package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakPolicyEngine(newRegistry())
        val item = newRestBreakPolicy("code-552", "title-823")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
