package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDwellEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDwellEstimateEngine(newRegistry())
        val item = newManualDwellEstimate("code-555", "name-994")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
