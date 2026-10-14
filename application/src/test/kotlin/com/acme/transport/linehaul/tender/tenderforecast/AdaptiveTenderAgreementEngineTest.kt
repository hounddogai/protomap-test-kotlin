package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTenderAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTenderAgreementEngine(newRegistry())
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
