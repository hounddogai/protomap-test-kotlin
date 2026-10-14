package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectCheckpointEngine(newRegistry())
        val item = newDefectCheckpoint("code-432", "name-748")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
