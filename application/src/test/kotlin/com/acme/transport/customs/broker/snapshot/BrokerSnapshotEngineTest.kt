package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerSnapshotEngine(newRegistry())
        val item = newBrokerSnapshot("code-701", "label-845")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
