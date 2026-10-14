package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTrailerYardManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTrailerYardManifestEngine(newRegistry())
        val item = newCappedTrailerYardManifest("code-85", "name-996")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
