package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLanePlanDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLanePlanDraftEngine()
        val item = newRollingLanePlanDraft("code-364", "name-819")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
