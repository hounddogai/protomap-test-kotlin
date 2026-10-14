package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorPolicyEngine()
        val item = newDockDoorPolicy("code-194", "name-200")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
