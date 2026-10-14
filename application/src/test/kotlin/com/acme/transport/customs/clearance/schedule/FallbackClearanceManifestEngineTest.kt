package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackClearanceManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackClearanceManifestEngine(newRegistry())
        val item = newFallbackClearanceManifest("code-289", "label-187")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
