package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBackhaulManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBackhaulManifestEngine()
        val item = newManualBackhaulManifest("code-284", "name-715")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
