package com.acme.transport.terminal.traileryard.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardBatchEngine()
        val item = newTrailerYardBatch("code-564", "name-548")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
