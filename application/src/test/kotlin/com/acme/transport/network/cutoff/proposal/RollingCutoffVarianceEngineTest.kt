package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCutoffVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCutoffVarianceEngine(newRegistry())
        val item = newRollingCutoffVariance("code-889", "name-307")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
