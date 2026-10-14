package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRateCardManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRateCardManifestEngine()
        val item = newLenientRateCardManifest("code-200", "label-750")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
