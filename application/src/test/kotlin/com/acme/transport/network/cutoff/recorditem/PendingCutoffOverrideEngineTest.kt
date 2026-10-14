package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCutoffOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCutoffOverrideEngine(newRegistry())
        val item = newPendingCutoffOverride("code-863", "title-881")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
