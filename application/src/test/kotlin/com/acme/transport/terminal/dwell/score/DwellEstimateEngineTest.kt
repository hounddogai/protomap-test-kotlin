package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellEstimateEngine(newRegistry())
        val item = newDwellEstimate("code-266", "title-320")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
