package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDisputePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDisputePlanEngine(newRegistry())
        val item = newStrictDisputePlan("code-766", "title-425")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
