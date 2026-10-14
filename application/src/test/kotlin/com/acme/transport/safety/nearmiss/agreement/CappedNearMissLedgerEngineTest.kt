package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedNearMissLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedNearMissLedgerEngine(newRegistry())
        val item = newCappedNearMissLedger("code-668", "title-380")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
