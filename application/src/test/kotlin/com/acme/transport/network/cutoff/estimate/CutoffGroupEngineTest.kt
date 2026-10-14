package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffGroupEngine()
        val item = newCutoffGroup("code-922", "label-738")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
