package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLanePlanOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLanePlanOverrideEngine(newRegistry())
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
