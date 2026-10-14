package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCutoffLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCutoffLedgerEngine(newRegistry())
        val item = newFallbackCutoffLedger("code-919", "title-163")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
