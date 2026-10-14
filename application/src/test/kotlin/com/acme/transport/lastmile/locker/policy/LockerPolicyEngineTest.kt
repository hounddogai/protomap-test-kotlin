package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerPolicyEngine(newRegistry())
        val item = newLockerPolicy("code-77", "title-108")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
