package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTireDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTireDigestEngine(newRegistry())
        val item = newLegacyTireDigest("code-640", "label-692")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
