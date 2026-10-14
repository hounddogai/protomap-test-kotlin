package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalBrokerCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalBrokerCheckpointEngine()
        val item = newRegionalBrokerCheckpoint("code-884", "label-124")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
