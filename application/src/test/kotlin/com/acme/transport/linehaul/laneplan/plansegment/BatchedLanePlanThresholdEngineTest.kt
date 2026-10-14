package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLanePlanThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLanePlanThresholdEngine(newRegistry())
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
