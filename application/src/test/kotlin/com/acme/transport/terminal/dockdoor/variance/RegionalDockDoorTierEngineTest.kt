package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDockDoorTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDockDoorTierEngine(newRegistry())
        val item = newRegionalDockDoorTier("code-5", "label-813")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
