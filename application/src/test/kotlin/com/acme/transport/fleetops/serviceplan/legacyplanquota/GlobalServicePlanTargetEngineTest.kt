package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalServicePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalServicePlanTargetEngine(newRegistry())
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
