package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedHubRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedHubRecordEngine()
        val item = newBatchedHubRecord("code-107", "title-434")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
