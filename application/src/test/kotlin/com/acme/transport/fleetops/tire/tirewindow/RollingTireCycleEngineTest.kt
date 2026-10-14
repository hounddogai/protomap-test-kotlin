package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTireCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTireCycleEngine(newRegistry())
        val item = newRollingTireCycle("code-477", "title-277")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
