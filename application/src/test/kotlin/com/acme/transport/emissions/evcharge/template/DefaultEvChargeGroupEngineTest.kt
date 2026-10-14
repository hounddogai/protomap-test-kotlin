package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultEvChargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultEvChargeGroupEngine()
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
