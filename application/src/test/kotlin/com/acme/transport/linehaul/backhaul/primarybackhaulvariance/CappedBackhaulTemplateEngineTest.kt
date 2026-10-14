package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBackhaulTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBackhaulTemplateEngine()
        val item = newCappedBackhaulTemplate("code-651", "title-39")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
