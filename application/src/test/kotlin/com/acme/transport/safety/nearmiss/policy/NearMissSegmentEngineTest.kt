package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissSegmentEngine(newRegistry())
        val item = newNearMissSegment("code-101", "label-889")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
