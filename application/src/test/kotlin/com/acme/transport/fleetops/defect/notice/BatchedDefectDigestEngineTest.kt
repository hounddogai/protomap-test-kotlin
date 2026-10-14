package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDefectDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDefectDigestEngine(newRegistry())
        val item = newBatchedDefectDigest("code-218", "label-267")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
