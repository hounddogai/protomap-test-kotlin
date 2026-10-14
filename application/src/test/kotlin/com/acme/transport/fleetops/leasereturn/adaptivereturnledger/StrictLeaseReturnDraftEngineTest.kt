package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictLeaseReturnDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictLeaseReturnDraftEngine(newRegistry())
        val item = newStrictLeaseReturnDraft("code-885", "label-813")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
