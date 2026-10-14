package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterManifestEngine()
        val item = newStrictRosterManifest("code-62", "label-367")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
