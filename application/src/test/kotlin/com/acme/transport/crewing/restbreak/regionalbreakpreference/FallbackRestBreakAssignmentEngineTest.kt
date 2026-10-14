package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRestBreakAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRestBreakAssignmentEngine()
        val item = newFallbackRestBreakAssignment("code-504", "name-163")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
