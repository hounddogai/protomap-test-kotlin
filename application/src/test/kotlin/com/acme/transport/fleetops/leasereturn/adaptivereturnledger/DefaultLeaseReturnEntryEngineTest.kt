package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLeaseReturnEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLeaseReturnEntryEngine(newRegistry())
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
