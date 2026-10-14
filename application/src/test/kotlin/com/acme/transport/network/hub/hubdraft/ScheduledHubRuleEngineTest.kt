package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHubRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHubRuleEngine(newRegistry())
        val item = newScheduledHubRule("code-624", "name-275")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
