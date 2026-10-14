package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingWindowEngine(newRegistry())
        val item = newIdlingWindow("code-520", "title-166")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
