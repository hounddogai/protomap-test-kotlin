package com.acme.transport.network.cutoff.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCutoffManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCutoffManifestEngine()
        val item = newCappedCutoffManifest("code-532", "name-353")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
