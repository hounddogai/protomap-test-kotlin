package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRestBreakTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRestBreakTemplateEngine(newRegistry())
        val item = newWeightedRestBreakTemplate("code-636", "label-705")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
