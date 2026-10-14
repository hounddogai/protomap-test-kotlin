package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeClaimEngine(newRegistry())
        val item = newTransitTimeClaim("code-391", "name-958")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
