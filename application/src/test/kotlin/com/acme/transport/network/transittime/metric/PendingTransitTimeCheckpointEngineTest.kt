package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTransitTimeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTransitTimeCheckpointEngine(newRegistry())
        val item = newPendingTransitTimeCheckpoint("code-653", "title-925")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
