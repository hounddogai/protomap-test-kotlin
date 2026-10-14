package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTransitTimeLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTransitTimeLedgerEngine(newRegistry())
        val item = newGlobalTransitTimeLedger("code-27", "name-787")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
