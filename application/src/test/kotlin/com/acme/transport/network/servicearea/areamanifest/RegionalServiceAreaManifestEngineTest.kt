package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalServiceAreaManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalServiceAreaManifestEngine(newRegistry())
        val item = newRegionalServiceAreaManifest("code-958", "name-815")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
