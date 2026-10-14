package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldPreferenceEngine()
        val item = newHoldPreference("code-786", "name-443")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
