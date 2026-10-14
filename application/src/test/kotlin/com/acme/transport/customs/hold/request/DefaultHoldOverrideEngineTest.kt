package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultHoldOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultHoldOverrideEngine()
        val item = newDefaultHoldOverride("code-244", "label-110")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
