package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingHubLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingHubLedgerEngine()
        val item = newRollingHubLedger("code-692", "name-235")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
