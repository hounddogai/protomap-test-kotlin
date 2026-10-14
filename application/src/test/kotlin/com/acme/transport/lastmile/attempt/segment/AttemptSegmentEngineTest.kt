package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptSegmentEngine(newRegistry())
        val item = newAttemptSegment("code-8", "name-115")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
