package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCutoffTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCutoffTierEngine(newRegistry())
        val item = newGlobalCutoffTier("code-832", "name-37")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
