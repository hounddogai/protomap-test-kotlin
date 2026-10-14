package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCrossDockBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCrossDockBaselineEngine(newRegistry())
        val item = newTieredCrossDockBaseline("code-489", "name-213")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
