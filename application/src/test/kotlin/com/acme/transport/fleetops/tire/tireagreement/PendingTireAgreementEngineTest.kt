package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTireAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTireAgreementEngine(newRegistry())
        val item = newPendingTireAgreement("code-225", "title-127")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
