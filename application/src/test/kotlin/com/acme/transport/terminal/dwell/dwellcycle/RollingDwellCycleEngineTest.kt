package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDwellCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDwellCycleEngine(newRegistry())
        val item = newRollingDwellCycle("code-707", "title-293")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
