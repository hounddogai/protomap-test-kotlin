package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingMappingEngine(newRegistry())
        val item = newIdlingMapping("code-219", "title-128")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
