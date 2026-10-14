package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakCycleEngine()
        val item = newRestBreakCycle("code-211", "title-28")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
