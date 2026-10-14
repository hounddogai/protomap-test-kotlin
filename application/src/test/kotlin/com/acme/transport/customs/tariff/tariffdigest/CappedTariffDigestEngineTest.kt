package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTariffDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTariffDigestEngine(newRegistry())
        val item = newCappedTariffDigest("code-555", "title-871")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
