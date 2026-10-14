package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeTemplateEngine(newRegistry())
        val item = newDisputeTemplate("code-527", "name-712")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
