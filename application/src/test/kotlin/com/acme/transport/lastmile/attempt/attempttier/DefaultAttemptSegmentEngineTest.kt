package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAttemptSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAttemptSegmentEngine(newRegistry())
        val item = newDefaultAttemptSegment("code-92", "label-758")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
