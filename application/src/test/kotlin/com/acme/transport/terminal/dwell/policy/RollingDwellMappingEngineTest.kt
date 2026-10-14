package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDwellMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDwellMappingEngine(newRegistry())
        val item = newRollingDwellMapping("code-578", "label-151")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
