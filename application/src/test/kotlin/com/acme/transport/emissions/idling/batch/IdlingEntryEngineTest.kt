package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingEntryEngine()
        val item = newIdlingEntry("code-192", "name-177")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
