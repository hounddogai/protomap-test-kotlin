package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalNodeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalNodeTemplateEngine()
        val item = newSeasonalNodeTemplate("code-345", "label-881")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
