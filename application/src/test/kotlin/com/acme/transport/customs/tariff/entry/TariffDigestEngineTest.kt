package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffDigestEngine()
        val item = newTariffDigest("code-620", "title-677")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
