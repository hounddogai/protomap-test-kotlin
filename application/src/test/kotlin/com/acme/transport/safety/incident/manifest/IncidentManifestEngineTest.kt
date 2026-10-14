package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentManifestEngine(newRegistry())
        val item = newIncidentManifest("code-167", "title-177")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
