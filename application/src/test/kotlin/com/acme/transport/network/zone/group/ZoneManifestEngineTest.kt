package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneManifestEngine()
        val item = newZoneManifest("code-445", "name-260")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
