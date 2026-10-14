package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDwellGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDwellGroupEngine()
        val item = newAdaptiveDwellGroup("code-869", "name-433")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
