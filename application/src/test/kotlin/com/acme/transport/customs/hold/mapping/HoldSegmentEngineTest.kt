package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldSegmentEngine(newRegistry())
        val item = newHoldSegment("code-8", "title-9")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
