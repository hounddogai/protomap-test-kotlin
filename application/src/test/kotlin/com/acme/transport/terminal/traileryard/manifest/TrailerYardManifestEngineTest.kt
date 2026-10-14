package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardManifestEngine()
        val item = newTrailerYardManifest("code-105", "title-890")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
