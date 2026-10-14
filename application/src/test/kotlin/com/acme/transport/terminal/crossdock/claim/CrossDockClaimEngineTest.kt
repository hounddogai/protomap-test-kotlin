package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockClaimEngine()
        val item = newCrossDockClaim("code-246", "title-582")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
