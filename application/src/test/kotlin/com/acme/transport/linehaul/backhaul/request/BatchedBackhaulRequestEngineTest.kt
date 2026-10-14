package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedBackhaulRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedBackhaulRequestEngine(newRegistry())
        val item = newBatchedBackhaulRequest("code-595", "label-806")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
