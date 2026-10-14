package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanWindowEngine(newRegistry())
        val item = newLanePlanWindow("code-207", "name-966")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
