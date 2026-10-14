package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServicePlanLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServicePlanLedgerEngine(newRegistry())
        val item = newPendingServicePlanLedger("code-385", "label-446")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
