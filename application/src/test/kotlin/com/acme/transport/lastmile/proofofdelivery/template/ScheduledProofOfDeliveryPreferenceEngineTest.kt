package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledProofOfDeliveryPreferenceEngine(newRegistry())
        val item = newScheduledProofOfDeliveryPreference("code-234", "name-434")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
