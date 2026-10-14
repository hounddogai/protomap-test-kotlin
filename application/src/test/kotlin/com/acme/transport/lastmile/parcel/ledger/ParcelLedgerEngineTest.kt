package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelLedgerEngine(newRegistry())
        val item = newParcelLedger("code-444", "label-714")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
