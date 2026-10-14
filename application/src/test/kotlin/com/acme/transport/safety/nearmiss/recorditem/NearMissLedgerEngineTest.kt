package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissLedgerEngine(newRegistry())
        val item = newNearMissLedger("code-318", "name-997")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
