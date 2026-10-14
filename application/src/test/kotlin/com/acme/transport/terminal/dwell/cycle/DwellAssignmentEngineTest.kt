package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellAssignmentEngine(newRegistry())
        val item = newDwellAssignment("code-828", "label-263")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
