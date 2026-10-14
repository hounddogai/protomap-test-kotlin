package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalServicePlanBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalServicePlanBaselineEngine(newRegistry())
        val item = newRegionalServicePlanBaseline("code-497", "label-319")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
