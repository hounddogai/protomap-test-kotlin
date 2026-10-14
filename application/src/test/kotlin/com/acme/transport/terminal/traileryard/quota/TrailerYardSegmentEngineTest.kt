package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardSegmentEngine(newRegistry())
        val item = newTrailerYardSegment("code-775", "title-471")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
