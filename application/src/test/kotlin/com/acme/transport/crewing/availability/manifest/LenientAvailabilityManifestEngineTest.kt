package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAvailabilityManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAvailabilityManifestEngine(newRegistry())
        val item = newLenientAvailabilityManifest("code-942", "title-523")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
