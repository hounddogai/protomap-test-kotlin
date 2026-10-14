package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissOverrideEngine()
        val item = newNearMissOverride("code-184", "label-911")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
