package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanNoticeEngine()
        val item = newServicePlanNotice("code-825", "name-501")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
