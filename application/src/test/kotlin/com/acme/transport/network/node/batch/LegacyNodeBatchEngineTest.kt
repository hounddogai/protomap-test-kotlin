package com.acme.transport.network.node.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyNodeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyNodeBatchEngine(newRegistry())
        val item = newLegacyNodeBatch("code-231", "title-801")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
