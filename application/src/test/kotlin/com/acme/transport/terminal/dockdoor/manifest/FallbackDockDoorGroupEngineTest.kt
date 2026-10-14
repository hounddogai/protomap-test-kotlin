package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDockDoorGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDockDoorGroupEngine(newRegistry())
        val item = newFallbackDockDoorGroup("code-65", "title-386")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
