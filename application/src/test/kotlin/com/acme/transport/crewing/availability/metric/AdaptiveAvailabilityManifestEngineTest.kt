package com.acme.transport.crewing.availability.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAvailabilityManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAvailabilityManifestEngine()
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
