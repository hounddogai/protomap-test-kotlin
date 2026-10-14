package com.acme.transport.customs.broker.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredBrokerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredBrokerManifestEngine(newRegistry())
        val item = newTieredBrokerManifest("code-995", "title-809")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
