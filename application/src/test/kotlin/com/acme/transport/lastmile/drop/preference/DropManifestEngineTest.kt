package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropManifestEngine(newRegistry())
        val item = newDropManifest("code-919", "label-480")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
