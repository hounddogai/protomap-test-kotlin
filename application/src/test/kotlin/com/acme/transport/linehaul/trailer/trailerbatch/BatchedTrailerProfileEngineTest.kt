package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTrailerProfileEngine(newRegistry())
        val item = newBatchedTrailerProfile("code-516", "label-657")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
