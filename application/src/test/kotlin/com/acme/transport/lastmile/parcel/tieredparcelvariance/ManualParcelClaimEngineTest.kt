package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualParcelClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualParcelClaimEngine(newRegistry())
        val item = newManualParcelClaim("code-31", "name-750")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
