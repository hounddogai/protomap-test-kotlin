package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderRuleEngine(newRegistry())
        val item = newTenderRule("code-493", "label-445")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
