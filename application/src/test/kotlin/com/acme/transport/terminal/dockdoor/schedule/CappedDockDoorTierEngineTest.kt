package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDockDoorTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDockDoorTierEngine(newRegistry())
        val item = newCappedDockDoorTier("code-803", "title-568")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
