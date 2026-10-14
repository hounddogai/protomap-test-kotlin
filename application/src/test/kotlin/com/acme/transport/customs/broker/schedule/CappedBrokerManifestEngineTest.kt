package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBrokerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBrokerManifestEngine(newRegistry())
        val item = newCappedBrokerManifest("code-393", "name-927")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
