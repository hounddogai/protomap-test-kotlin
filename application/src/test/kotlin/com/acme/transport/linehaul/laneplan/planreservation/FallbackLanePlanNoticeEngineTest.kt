package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLanePlanNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLanePlanNoticeEngine(newRegistry())
        val item = newFallbackLanePlanNotice("code-34", "title-767")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
