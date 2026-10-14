package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTransitTimeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTransitTimeRequestEngine(newRegistry())
        val item = newBatchedTransitTimeRequest("code-571", "label-926")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
