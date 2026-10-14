package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireWindowEngine(newRegistry())
        val item = newTireWindow("code-629", "title-243")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
