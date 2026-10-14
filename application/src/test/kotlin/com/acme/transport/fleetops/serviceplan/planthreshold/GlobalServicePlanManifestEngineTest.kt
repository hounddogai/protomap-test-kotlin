package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalServicePlanManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalServicePlanManifestEngine(newRegistry())
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
