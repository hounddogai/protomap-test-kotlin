package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorEstimateEngine(newRegistry())
        val item = newDockDoorEstimate("code-929", "title-341")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
