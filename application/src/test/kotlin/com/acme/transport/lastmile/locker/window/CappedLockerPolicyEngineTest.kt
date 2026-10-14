package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLockerPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLockerPolicyEngine(newRegistry())
        val item = newCappedLockerPolicy("code-742", "name-480")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
