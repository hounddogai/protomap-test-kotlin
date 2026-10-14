package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaScheduleEngine()
        val item = newServiceAreaSchedule("code-692", "title-802")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
