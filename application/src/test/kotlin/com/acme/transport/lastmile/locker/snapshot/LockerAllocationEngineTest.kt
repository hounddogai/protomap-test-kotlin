package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerAllocationEngine(newRegistry())
        val item = newLockerAllocation("code-861", "name-498")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
