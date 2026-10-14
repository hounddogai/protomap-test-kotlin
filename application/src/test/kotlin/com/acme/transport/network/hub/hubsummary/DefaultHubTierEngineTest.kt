package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultHubTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultHubTierEngine(newRegistry())
        val item = newDefaultHubTier("code-671", "title-321")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
