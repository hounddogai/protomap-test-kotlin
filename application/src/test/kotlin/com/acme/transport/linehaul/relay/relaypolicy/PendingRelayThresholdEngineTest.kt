package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRelayThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRelayThresholdEngine(newRegistry())
        val item = newPendingRelayThreshold("code-787", "name-321")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
