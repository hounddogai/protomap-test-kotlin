package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDefectTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDefectTargetEngine(newRegistry())
        val item = newManualDefectTarget("code-967", "label-69")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
