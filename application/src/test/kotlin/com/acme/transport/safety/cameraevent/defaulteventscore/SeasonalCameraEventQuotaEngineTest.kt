package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCameraEventQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCameraEventQuotaEngine()
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
