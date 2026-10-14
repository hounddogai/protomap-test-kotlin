package com.acme.transport.safety.nearmiss.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictNearMissOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictNearMissOverrideEngine()
        val item = newStrictNearMissOverride("code-236", "name-627")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
