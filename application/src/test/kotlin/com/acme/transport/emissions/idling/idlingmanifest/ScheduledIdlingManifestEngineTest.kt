package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledIdlingManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledIdlingManifestEngine()
        val item = newScheduledIdlingManifest("code-122", "title-616")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
