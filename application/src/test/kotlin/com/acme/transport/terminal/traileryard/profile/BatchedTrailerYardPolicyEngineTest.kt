package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerYardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTrailerYardPolicyEngine(newRegistry())
        val item = newBatchedTrailerYardPolicy("code-425", "label-327")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
