package com.acme.transport.linehaul.tender.fallbacktenderbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTenderAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTenderAllocationEngine()
        val item = newRollingTenderAllocation("code-582", "label-383")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
