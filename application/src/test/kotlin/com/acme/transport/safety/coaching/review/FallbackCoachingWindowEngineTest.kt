package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCoachingWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCoachingWindowEngine(newRegistry())
        val item = newFallbackCoachingWindow("code-436", "title-981")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
