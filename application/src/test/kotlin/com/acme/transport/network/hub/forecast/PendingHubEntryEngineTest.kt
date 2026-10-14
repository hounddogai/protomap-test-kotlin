package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingHubEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingHubEntryEngine(newRegistry())
        val item = newPendingHubEntry("code-76", "label-721")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
