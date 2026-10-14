package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectSnapshotEngine(newRegistry())
        val item = newDefectSnapshot("code-364", "name-752")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
