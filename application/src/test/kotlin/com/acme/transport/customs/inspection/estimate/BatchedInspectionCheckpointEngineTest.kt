package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedInspectionCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedInspectionCheckpointEngine(newRegistry())
        val item = newBatchedInspectionCheckpoint("code-328", "title-307")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
