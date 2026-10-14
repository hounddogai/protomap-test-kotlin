package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffTierEngine(newRegistry())
        val item = newCutoffTier("code-888", "label-658")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
