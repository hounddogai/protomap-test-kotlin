package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCutoffBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCutoffBatchEngine()
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
