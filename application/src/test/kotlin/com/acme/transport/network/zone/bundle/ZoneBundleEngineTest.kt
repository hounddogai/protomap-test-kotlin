package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneBundleEngine()
        val item = newZoneBundle("code-336", "title-99")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
