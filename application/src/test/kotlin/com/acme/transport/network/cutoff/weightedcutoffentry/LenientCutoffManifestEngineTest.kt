package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCutoffManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCutoffManifestEngine(newRegistry())
        val item = newLenientCutoffManifest("code-358", "name-46")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
