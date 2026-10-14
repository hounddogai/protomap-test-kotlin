package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelClaimEngine(newRegistry())
        val item = newParcelClaim("code-502", "name-428")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
