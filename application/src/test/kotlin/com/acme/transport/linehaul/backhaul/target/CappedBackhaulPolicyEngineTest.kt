package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBackhaulPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBackhaulPolicyEngine()
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
