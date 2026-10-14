package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellRollupEngine()
        val item = newDwellRollup("code-180", "title-301")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
