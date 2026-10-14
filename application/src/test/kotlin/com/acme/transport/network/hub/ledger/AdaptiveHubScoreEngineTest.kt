package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveHubScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveHubScoreEngine(newRegistry())
        val item = newAdaptiveHubScore("code-788", "title-379")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
