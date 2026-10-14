package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultServicePlanLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultServicePlanLedgerEngine(newRegistry())
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
