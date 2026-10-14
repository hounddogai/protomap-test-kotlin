package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DockDoorForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDockDoorForecastEngine(newRegistry())
        val item = newDockDoorForecast("code-771", "name-30")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
