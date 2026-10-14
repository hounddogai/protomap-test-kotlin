package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientIdlingSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientIdlingSlotEngine(newRegistry())
        val item = newLenientIdlingSlot("code-183", "label-371")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
