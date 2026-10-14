package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveZoneAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveZoneAgreementEngine()
        val item = newAdaptiveZoneAgreement("code-849", "label-419")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
