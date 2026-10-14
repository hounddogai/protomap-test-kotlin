package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationAgreementEngine(newRegistry())
        val item = newUtilizationAgreement("code-352", "title-38")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
