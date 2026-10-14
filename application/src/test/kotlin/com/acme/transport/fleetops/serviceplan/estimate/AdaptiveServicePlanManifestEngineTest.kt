package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveServicePlanManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveServicePlanManifestEngine(newRegistry())
        val item = newAdaptiveServicePlanManifest("code-627", "title-742")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
