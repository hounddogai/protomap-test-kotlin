package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNearMissMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNearMissMappingEngine(newRegistry())
        val item = newBatchedNearMissMapping("code-384", "name-350")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
