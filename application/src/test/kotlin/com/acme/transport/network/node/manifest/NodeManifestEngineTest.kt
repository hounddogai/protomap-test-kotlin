package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class NodeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeManifestEngine()
        val item = newNodeManifest("code-185", "title-965")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
