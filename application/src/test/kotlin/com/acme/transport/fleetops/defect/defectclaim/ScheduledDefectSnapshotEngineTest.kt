package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectSnapshotEngine(newRegistry())
        val item = newScheduledDefectSnapshot("code-954", "label-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
