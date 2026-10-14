package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingPreferenceEngine()
        val item = newIdlingPreference("code-745", "title-357")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
