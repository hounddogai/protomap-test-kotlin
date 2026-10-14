package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRedeliveryWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRedeliveryWindowEngine()
        val item = newManualRedeliveryWindow("code-916", "name-756")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
