package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDockDoorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDockDoorExceptionEngine()
        val item = newPendingDockDoorException("code-826", "title-540")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
