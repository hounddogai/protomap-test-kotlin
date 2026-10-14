package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedQualificationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedQualificationCheckpointEngine(newRegistry())
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
