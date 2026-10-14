package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTireAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTireAgreementEngine(newRegistry())
        val item = newManualTireAgreement("code-988", "label-301")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
