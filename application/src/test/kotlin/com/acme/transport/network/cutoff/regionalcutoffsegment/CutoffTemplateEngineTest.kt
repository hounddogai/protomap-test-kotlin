package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffTemplateEngine()
        val item = newCutoffTemplate("code-638", "label-457")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
