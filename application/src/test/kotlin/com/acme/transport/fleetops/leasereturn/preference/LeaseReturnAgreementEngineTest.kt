package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class LeaseReturnAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLeaseReturnAgreementEngine()
        val item = newLeaseReturnAgreement("code-530", "title-71")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
