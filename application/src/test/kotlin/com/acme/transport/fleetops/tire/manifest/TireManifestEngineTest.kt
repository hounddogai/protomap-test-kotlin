package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireManifestEngine(newRegistry())
        val item = newTireManifest("code-58", "name-355")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
