package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDefectSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDefectSnapshotEngine()
        val item = newGlobalDefectSnapshot("code-957", "name-158")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
