package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectTargetEngine(newRegistry())
        val item = newDefectTarget("code-505", "title-154")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
