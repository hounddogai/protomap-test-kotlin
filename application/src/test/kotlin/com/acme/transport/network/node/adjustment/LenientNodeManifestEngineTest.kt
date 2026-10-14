package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNodeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNodeManifestEngine(newRegistry())
        val item = newLenientNodeManifest("code-783", "name-868")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
