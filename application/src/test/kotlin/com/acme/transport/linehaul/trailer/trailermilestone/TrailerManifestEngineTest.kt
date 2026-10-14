package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerManifestEngine(newRegistry())
        val item = newTrailerManifest("code-806", "title-900")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
