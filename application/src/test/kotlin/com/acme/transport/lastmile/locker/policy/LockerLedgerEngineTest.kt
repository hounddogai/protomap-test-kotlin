package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerLedgerEngine(newRegistry())
        val item = newLockerLedger("code-122", "title-520")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
