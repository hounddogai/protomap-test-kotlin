package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCapacityManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCapacityManifestEngine()
        val item = newRegionalCapacityManifest("code-9", "title-394")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
