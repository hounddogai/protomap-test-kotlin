package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledServiceAreaNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledServiceAreaNoticeEngine(newRegistry())
        val item = newScheduledServiceAreaNotice("code-887", "name-352")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
