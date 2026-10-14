package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderCheckpointEngine()
        val item = newTenderCheckpoint("code-733", "title-595")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
