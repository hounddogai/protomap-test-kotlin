package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBrokerDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBrokerDigestEngine()
        val item = newManualBrokerDigest("code-658", "label-7")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
