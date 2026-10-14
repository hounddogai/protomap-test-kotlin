package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRedeliveryWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRedeliveryWindowEngine()
        val item = newPendingRedeliveryWindow("code-570", "name-160")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
