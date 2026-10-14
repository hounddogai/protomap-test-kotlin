package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalScorecardManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalScorecardManifestEngine()
        val item = newGlobalScorecardManifest("code-667", "name-348")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
