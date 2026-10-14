package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingScorecardGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingScorecardGroupEngine(newRegistry())
        val item = newRollingScorecardGroup("code-509", "title-479")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
