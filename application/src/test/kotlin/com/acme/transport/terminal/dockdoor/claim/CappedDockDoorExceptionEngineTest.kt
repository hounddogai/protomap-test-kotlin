package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDockDoorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDockDoorExceptionEngine(newRegistry())
        val item = newCappedDockDoorException("code-23", "title-442")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
