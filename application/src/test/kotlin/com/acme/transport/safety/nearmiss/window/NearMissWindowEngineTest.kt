package com.acme.transport.safety.nearmiss.window

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissWindowEngine()
        val item = newNearMissWindow("code-970", "title-5")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
