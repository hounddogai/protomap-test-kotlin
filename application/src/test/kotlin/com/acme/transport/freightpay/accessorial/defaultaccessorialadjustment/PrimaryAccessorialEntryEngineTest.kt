package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAccessorialEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAccessorialEntryEngine()
        val item = newPrimaryAccessorialEntry("code-540", "name-532")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
