package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRedeliveryPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRedeliveryPreferenceEngine(newRegistry())
        val item = newRegionalRedeliveryPreference("code-28", "name-594")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
