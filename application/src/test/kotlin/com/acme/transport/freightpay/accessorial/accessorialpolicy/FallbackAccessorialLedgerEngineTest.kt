package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAccessorialLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAccessorialLedgerEngine(newRegistry())
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
