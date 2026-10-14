package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingRecordEngine()
        val item = newIdlingRecord("code-195", "label-19")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
