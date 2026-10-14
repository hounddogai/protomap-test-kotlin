package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientLanePlanRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientLanePlanRuleEngine(newRegistry())
        val item = newLenientLanePlanRule("code-396", "title-352")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
