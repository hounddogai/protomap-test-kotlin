package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDisputeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDisputeSnapshotEngine(newRegistry())
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
