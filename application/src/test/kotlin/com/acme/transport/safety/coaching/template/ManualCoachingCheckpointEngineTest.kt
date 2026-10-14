package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCoachingCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCoachingCheckpointEngine()
        val item = newManualCoachingCheckpoint("code-354", "label-315")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
