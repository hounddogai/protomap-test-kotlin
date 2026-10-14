package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanRollupEngine(newRegistry())
        val item = newLanePlanRollup("code-103", "title-861")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
