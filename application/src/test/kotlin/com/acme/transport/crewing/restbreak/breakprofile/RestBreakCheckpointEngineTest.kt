package com.acme.transport.crewing.restbreak.breakprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakCheckpointEngine()
        val item = newRestBreakCheckpoint("code-134", "title-913")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
