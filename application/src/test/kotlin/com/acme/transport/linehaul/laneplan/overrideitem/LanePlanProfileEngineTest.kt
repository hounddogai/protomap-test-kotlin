package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanProfileEngine()
        val item = newLanePlanProfile("code-296", "label-924")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
