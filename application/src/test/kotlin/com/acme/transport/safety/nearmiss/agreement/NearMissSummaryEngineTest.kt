package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissSummaryEngine(newRegistry())
        val item = newNearMissSummary("code-343", "name-300")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
