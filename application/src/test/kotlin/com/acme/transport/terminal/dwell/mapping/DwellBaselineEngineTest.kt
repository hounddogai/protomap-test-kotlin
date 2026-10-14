package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellBaselineEngine(newRegistry())
        val item = newDwellBaseline("code-736", "title-107")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
