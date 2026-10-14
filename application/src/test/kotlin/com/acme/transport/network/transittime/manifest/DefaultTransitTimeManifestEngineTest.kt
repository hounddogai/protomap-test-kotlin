package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTransitTimeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTransitTimeManifestEngine(newRegistry())
        val item = newDefaultTransitTimeManifest("code-777", "title-67")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
