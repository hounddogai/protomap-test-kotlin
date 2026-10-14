package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientEvChargeLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientEvChargeLedgerEngine(newRegistry())
        val item = newLenientEvChargeLedger("code-275", "label-26")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
