package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualLeaseReturnEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualLeaseReturnEntryEngine(newRegistry())
        val item = newManualLeaseReturnEntry("code-442", "label-223")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
