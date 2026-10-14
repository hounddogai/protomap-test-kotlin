package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDockDoorTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDockDoorTierEngine(newRegistry())
        val item = newPendingDockDoorTier("code-553", "label-903")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
