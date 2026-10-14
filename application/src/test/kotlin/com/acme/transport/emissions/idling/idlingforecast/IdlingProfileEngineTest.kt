package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingProfileEngine()
        val item = newIdlingProfile("code-805", "label-368")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
