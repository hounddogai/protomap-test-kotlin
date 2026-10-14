package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDefectSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDefectSnapshotEngine()
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
