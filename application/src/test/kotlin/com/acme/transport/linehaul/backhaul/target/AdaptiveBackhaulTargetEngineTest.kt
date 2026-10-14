package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveBackhaulTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveBackhaulTargetEngine(newRegistry())
        val item = newAdaptiveBackhaulTarget("code-292", "label-103")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
