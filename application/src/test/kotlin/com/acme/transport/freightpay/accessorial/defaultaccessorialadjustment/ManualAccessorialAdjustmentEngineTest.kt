package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAccessorialAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAccessorialAdjustmentEngine()
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
