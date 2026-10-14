package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDockDoorGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDockDoorGroupEngine(newRegistry())
        val item = newGlobalDockDoorGroup("code-686", "name-193")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
