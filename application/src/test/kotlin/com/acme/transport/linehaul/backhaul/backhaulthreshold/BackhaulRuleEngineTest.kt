package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulRuleEngine()
        val item = newBackhaulRule("code-313", "name-931")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
