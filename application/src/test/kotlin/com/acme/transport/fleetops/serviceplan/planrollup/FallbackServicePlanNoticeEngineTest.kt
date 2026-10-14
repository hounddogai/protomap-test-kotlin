package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServicePlanNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServicePlanNoticeEngine(newRegistry())
        val item = newFallbackServicePlanNotice("code-669", "title-771")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
