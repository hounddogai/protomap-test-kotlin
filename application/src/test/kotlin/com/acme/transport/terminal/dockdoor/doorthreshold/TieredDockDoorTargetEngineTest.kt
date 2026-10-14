package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDockDoorTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDockDoorTargetEngine(newRegistry())
        val item = newTieredDockDoorTarget("code-384", "label-582")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
