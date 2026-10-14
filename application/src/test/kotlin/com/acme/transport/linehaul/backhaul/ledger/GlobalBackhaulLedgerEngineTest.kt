package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBackhaulLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBackhaulLedgerEngine(newRegistry())
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
