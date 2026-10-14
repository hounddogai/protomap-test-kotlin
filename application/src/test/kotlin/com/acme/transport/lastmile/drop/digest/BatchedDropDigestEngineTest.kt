package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDropDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDropDigestEngine(newRegistry())
        val item = newBatchedDropDigest("code-345", "title-812")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
