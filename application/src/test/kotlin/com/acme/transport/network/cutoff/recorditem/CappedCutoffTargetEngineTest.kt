package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCutoffTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCutoffTargetEngine(newRegistry())
        val item = newCappedCutoffTarget("code-284", "label-739")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
