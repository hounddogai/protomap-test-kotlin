package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBrokerCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBrokerCheckpointEngine()
        val item = newPendingBrokerCheckpoint("code-961", "name-940")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
