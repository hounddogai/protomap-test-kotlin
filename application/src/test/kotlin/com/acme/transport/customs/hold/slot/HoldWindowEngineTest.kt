package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldWindowEngine()
        val item = newHoldWindow("code-273", "name-625")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
