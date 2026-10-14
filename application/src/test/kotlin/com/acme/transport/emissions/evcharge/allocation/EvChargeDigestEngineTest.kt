package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeDigestEngine()
        val item = newEvChargeDigest("code-382", "name-668")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
