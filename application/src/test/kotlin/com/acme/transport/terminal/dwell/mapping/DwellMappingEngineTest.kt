package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellMappingEngine()
        val item = newDwellMapping("code-384", "title-597")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
