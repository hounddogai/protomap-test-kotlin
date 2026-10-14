package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerTargetEngine(newRegistry())
        val item = newLockerTarget("code-125", "name-130")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
