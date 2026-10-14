package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubSegmentEngine(newRegistry())
        val item = newHubSegment("code-700", "title-233")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
