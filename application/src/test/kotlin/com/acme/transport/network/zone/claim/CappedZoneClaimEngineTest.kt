package com.acme.transport.network.zone.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedZoneClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedZoneClaimEngine(newRegistry())
        val item = newCappedZoneClaim("code-767", "name-127")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
