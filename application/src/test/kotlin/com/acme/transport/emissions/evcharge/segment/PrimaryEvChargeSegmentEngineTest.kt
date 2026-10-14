package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEvChargeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEvChargeSegmentEngine(newRegistry())
        val item = newPrimaryEvChargeSegment("code-646", "label-24")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
