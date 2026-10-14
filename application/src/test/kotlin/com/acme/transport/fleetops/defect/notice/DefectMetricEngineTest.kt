package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectMetricEngine(newRegistry())
        val item = newDefectMetric("code-408", "name-609")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
