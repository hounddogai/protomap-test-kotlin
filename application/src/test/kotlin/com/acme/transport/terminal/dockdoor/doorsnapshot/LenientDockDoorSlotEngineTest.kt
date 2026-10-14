package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDockDoorSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDockDoorSlotEngine(newRegistry())
        val item = newLenientDockDoorSlot("code-699", "label-377")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
