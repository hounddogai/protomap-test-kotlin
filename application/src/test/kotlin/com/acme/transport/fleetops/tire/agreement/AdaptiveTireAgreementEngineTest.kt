package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTireAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTireAgreementEngine(newRegistry())
        val item = newAdaptiveTireAgreement("code-196", "title-642")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
