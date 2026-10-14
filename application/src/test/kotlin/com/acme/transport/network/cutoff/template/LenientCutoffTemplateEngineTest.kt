package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCutoffTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCutoffTemplateEngine()
        val item = newLenientCutoffTemplate("code-492", "title-113")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
