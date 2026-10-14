package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingSegmentEngine(newRegistry())
        val item = newIdlingSegment("code-454", "name-83")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
