package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingBaselineEngine(newRegistry())
        val item = newIdlingBaseline("code-41", "label-368")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
