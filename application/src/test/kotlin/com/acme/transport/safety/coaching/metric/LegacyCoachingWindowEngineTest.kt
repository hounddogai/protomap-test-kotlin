package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCoachingWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCoachingWindowEngine(newRegistry())
        val item = newLegacyCoachingWindow("code-919", "label-816")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
