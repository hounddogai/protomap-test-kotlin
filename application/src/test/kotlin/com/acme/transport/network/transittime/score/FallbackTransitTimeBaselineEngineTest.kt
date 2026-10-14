package com.acme.transport.network.transittime.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTransitTimeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTransitTimeBaselineEngine(newRegistry())
        val item = newFallbackTransitTimeBaseline("code-814", "name-397")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
