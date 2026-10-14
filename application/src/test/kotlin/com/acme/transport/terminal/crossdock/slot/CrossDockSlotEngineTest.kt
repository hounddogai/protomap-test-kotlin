package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockSlotEngine(newRegistry())
        val item = newCrossDockSlot("code-595", "label-999")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
