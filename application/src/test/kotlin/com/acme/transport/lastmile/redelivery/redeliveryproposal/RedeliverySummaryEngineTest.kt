package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliverySummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliverySummaryEngine(newRegistry())
        val item = newRedeliverySummary("code-395", "label-307")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
