package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualLanePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualLanePlanTargetEngine(newRegistry())
        val item = newManualLanePlanTarget("code-743", "label-565")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
