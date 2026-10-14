package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerManifestEngine(newRegistry())
        val item = newBrokerManifest("code-945", "name-905")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
