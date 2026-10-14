package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCutoffPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCutoffPolicyEngine(newRegistry())
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
