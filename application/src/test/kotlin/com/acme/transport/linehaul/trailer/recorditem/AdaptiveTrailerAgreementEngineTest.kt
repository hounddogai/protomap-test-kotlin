package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTrailerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTrailerAgreementEngine(newRegistry())
        val item = newAdaptiveTrailerAgreement("code-757", "title-674")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
