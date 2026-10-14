package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDisputeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDisputeTemplateEngine(newRegistry())
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
