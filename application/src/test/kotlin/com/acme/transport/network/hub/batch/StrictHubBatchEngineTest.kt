package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictHubBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictHubBatchEngine()
        val item = newStrictHubBatch("code-999", "name-864")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
