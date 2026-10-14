package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingScoreEngine(newRegistry())
        val item = newIdlingScore("code-277", "label-43")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
