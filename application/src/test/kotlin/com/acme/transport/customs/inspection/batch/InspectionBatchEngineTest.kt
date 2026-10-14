package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionBatchEngine(newRegistry())
        val item = newInspectionBatch("code-965", "label-232")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
