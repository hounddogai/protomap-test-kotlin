package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTariffAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTariffAgreementEngine(newRegistry())
        val item = newPendingTariffAgreement("code-336", "title-185")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
