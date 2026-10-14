package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalViolationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalViolationSegmentEngine(newRegistry())
        val item = newGlobalViolationSegment("code-169", "title-248")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
