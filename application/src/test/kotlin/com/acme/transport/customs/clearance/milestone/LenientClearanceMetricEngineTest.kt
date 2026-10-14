package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientClearanceMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientClearanceMetricEngine(newRegistry())
        val item = newLenientClearanceMetric("code-216", "title-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
