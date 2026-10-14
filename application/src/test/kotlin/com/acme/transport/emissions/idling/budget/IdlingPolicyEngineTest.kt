package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingPolicyEngine(newRegistry())
        val item = newIdlingPolicy("code-907", "title-439")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
