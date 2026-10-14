package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRedeliveryEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRedeliveryEntryEngine()
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
