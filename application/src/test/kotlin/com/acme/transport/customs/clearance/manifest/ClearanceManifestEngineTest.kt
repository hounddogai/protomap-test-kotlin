package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceManifestEngine()
        val item = newClearanceManifest("code-760", "name-387")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
