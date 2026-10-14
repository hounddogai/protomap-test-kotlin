package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerPolicyEngine(newRegistry())
        val item = newTrailerPolicy("code-729", "label-134")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
