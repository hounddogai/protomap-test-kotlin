package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTrailerYardManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTrailerYardManifestEngine(newRegistry())
        val item = newRegionalTrailerYardManifest("code-185", "label-800")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
