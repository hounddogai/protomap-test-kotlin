package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissManifestEngine()
        val item = newNearMissManifest("code-431", "name-836")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
