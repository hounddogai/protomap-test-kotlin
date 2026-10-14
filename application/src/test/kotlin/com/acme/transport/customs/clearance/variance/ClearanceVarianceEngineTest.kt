package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceVarianceEngine(newRegistry())
        val item = newClearanceVariance("code-643", "label-257")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
