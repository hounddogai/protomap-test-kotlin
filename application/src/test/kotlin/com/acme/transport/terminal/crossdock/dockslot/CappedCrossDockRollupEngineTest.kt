package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCrossDockRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCrossDockRollupEngine()
        val item = newCappedCrossDockRollup("code-397", "name-950")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
