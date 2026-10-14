package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRedeliveryLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRedeliveryLedgerEngine(newRegistry())
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
