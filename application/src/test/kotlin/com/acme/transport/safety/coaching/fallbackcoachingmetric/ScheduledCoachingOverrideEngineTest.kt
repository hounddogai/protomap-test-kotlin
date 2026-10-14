package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCoachingOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCoachingOverrideEngine()
        val item = newScheduledCoachingOverride("code-591", "name-114")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
