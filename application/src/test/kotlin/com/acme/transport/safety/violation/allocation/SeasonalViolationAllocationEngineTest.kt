package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalViolationAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalViolationAllocationEngine(newRegistry())
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
