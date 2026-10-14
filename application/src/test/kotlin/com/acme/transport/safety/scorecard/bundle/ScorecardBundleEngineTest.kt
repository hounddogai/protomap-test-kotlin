package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardBundleEngine(newRegistry())
        val item = newScorecardBundle("code-288", "label-205")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
