package com.acme.transport.network.servicearea.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaLedgerEngine()
        val item = newServiceAreaLedger("code-960", "title-66")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
