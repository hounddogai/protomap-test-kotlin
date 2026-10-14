package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServicePlanProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServicePlanProfileEngine(newRegistry())
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
