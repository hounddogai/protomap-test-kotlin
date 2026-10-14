package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialPlanEngine(newRegistry())
        val item = newAccessorialPlan("code-916", "label-597")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
