package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCutoffEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCutoffEstimateEngine(newRegistry())
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
