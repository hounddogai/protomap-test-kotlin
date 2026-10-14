package com.acme.transport.emissions.evcharge.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeManifestEngine()
        val item = newEvChargeManifest("code-77", "title-496")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
