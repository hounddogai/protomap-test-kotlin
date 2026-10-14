package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTenderCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTenderCheckpointEngine()
        val item = newLenientTenderCheckpoint("code-432", "label-303")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
