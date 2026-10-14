package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTariffClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTariffClaimEngine(newRegistry())
        val item = newPendingTariffClaim("code-661", "label-703")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
