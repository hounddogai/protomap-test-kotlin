package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardMappingEngine(newRegistry())
        val item = newScorecardMapping("code-846", "label-27")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
