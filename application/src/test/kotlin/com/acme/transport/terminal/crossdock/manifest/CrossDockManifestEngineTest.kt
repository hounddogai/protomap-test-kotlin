package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockManifestEngine(newRegistry())
        val item = newCrossDockManifest("code-314", "title-494")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
