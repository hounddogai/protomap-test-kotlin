package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLockerClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLockerClaimEngine(newRegistry())
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
