package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientScorecardSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientScorecardSlotEngine(newRegistry())
        val item = newLenientScorecardSlot("code-115", "name-839")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
