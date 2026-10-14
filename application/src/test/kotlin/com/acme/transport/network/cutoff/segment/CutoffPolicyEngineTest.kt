package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffPolicyEngine(newRegistry())
        val item = newCutoffPolicy("code-86", "title-456")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
