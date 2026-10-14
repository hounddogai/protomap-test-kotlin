package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderSnapshotEngine(newRegistry())
        val item = newTenderSnapshot("code-215", "label-503")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
