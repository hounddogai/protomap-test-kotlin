package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class DropSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropSegmentEngine()
        val item = newDropSegment("code-899", "name-842")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
