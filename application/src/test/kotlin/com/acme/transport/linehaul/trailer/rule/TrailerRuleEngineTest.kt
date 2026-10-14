package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerRuleEngine(newRegistry())
        val item = newTrailerRule("code-827", "label-513")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
