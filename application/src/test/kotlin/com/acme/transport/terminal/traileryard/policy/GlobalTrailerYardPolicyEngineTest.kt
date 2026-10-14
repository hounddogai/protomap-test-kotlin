package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTrailerYardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTrailerYardPolicyEngine(newRegistry())
        val item = newGlobalTrailerYardPolicy("code-513", "label-819")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
