package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNearMissClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNearMissClaimEngine(newRegistry())
        val item = newLenientNearMissClaim("code-534", "title-762")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
