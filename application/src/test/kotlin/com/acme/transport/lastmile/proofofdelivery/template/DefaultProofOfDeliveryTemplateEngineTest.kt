package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultProofOfDeliveryTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultProofOfDeliveryTemplateEngine()
        val item = newDefaultProofOfDeliveryTemplate("code-266", "name-822")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
