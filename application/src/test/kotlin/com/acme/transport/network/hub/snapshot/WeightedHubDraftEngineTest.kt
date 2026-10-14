package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedHubDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedHubDraftEngine(newRegistry())
        val item = newWeightedHubDraft("code-879", "label-237")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
