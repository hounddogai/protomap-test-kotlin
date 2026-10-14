package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRedeliveryOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRedeliveryOverrideEngine()
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
