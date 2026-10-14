package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockAdjustmentEngine(newRegistry())
        val item = newCrossDockAdjustment("code-226", "name-720")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
