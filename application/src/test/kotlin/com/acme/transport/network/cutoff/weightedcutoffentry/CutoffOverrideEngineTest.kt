package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffOverrideEngine(newRegistry())
        val item = newCutoffOverride("code-351", "label-829")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
