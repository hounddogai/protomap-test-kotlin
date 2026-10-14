package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakSegmentEngine(newRegistry())
        val item = newRestBreakSegment("code-316", "label-190")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
