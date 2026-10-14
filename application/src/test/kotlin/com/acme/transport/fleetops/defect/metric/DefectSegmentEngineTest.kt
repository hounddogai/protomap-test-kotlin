package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectSegmentEngine(newRegistry())
        val item = newDefectSegment("code-243", "label-348")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
