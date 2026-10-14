package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveServicePlanNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveServicePlanNoticeEngine()
        val item = newAdaptiveServicePlanNotice("code-389", "label-747")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
