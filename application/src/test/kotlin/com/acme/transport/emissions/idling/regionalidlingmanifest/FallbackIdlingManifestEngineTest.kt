package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackIdlingManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackIdlingManifestEngine(newRegistry())
        val item = newFallbackIdlingManifest("code-384", "title-653")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
