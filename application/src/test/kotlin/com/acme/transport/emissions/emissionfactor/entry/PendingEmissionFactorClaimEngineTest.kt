package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingEmissionFactorClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingEmissionFactorClaimEngine(newRegistry())
        val item = newPendingEmissionFactorClaim("code-957", "label-663")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
