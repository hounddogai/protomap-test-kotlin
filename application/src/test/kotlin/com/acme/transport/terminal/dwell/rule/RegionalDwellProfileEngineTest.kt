package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDwellProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDwellProfileEngine()
        val item = newRegionalDwellProfile("code-99", "title-267")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
