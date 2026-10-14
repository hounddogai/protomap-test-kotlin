package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakManifestEngine()
        val item = newRestBreakManifest("code-867", "label-109")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
