package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEvChargeRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEvChargeRecordEngine()
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
