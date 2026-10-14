package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDefectCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDefectCheckpointEngine()
        val item = newDefaultDefectCheckpoint("code-29", "title-642")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
