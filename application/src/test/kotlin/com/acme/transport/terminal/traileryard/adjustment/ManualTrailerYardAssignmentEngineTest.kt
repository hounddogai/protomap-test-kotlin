package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerYardAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerYardAssignmentEngine(newRegistry())
        val item = newManualTrailerYardAssignment("code-982", "title-436")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
