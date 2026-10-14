package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorReviewEngine(newRegistry())
        val item = newDockDoorReview("code-869", "name-879")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
