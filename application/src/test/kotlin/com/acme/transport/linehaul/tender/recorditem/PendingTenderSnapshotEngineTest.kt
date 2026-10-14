package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTenderSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTenderSnapshotEngine(newRegistry())
        val item = newPendingTenderSnapshot("code-260", "label-375")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
