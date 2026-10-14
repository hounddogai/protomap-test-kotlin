package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalHoldPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalHoldPolicyEngine(newRegistry())
        val item = newGlobalHoldPolicy("code-562", "name-767")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
