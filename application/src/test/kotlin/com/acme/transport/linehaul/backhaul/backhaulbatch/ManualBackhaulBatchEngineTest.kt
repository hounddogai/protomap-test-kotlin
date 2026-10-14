package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBackhaulBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBackhaulBatchEngine()
        val item = newManualBackhaulBatch("code-746", "label-383")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
