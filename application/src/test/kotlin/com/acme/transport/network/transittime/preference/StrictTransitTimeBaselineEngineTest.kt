package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTransitTimeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTransitTimeBaselineEngine(newRegistry())
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
