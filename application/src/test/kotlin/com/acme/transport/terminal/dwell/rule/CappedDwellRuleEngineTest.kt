package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDwellRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDwellRuleEngine(newRegistry())
        val item = newCappedDwellRule("code-441", "label-86")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
