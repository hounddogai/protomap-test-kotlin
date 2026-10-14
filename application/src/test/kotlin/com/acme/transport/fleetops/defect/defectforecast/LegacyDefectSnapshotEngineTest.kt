package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDefectSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDefectSnapshotEngine(newRegistry())
        val item = newLegacyDefectSnapshot("code-413", "name-823")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
