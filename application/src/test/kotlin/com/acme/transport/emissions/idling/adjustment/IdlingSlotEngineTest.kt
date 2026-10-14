package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingSlotEngine(newRegistry())
        val item = newIdlingSlot("code-805", "title-660")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
