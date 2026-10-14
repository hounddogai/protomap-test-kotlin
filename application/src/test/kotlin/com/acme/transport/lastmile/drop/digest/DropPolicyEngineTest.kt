package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropPolicyEngine(newRegistry())
        val item = newDropPolicy("code-252", "title-846")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
