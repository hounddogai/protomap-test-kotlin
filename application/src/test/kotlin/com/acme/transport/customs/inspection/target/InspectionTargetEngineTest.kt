package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionTargetEngine(newRegistry())
        val item = newInspectionTarget("code-375", "title-707")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
