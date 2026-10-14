package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackAccessorialMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackAccessorialMilestoneEngine(newRegistry())
        val item = newFallbackAccessorialMilestone("code-784", "title-160")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
