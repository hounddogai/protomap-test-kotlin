package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTireQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTireQuotaEngine()
        val item = newDefaultTireQuota("code-465", "label-692")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
