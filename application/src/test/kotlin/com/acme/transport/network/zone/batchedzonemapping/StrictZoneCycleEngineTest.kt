package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictZoneCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictZoneCycleEngine()
        val item = newStrictZoneCycle("code-515", "label-713")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
