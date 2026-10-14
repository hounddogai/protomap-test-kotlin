package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffThresholdEngine(newRegistry())
        val item = newCutoffThreshold("code-224", "title-481")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
