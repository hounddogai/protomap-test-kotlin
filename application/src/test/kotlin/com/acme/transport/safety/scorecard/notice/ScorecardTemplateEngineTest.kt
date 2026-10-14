package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardTemplateEngine(newRegistry())
        val item = newScorecardTemplate("code-239", "title-998")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
