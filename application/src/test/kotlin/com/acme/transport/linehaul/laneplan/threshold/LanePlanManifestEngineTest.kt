package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanManifestEngine(newRegistry())
        val item = newLanePlanManifest("code-283", "title-828")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
