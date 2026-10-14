package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDwellAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDwellAllocationEngine(newRegistry())
        val item = newScheduledDwellAllocation("code-129", "title-321")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
