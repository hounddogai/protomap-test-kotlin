package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffRuleEngine(newRegistry())
        val item = newCutoffRule("code-744", "title-244")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
