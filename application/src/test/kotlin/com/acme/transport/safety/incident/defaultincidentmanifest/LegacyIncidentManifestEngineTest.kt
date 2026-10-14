package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIncidentManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIncidentManifestEngine(newRegistry())
        val item = newLegacyIncidentManifest("code-980", "title-202")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
