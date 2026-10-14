package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRosterCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRosterCycleEngine(newRegistry())
        val item = newFallbackRosterCycle("code-238", "title-280")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
