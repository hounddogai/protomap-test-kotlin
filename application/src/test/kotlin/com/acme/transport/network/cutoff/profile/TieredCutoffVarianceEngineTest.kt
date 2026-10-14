package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCutoffVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCutoffVarianceEngine(newRegistry())
        val item = newTieredCutoffVariance("code-889", "title-777")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
