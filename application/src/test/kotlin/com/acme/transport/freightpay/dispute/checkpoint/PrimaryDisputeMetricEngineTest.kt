package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDisputeMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDisputeMetricEngine(newRegistry())
        val item = newPrimaryDisputeMetric("code-624", "label-58")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
