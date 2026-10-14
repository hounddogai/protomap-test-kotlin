package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRedeliveryPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRedeliveryPreferenceEngine()
        val item = newPrimaryRedeliveryPreference("code-846", "name-810")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
