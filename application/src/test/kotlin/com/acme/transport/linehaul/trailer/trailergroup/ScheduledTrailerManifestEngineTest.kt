package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTrailerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTrailerManifestEngine()
        val item = newScheduledTrailerManifest("code-1", "title-63")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
