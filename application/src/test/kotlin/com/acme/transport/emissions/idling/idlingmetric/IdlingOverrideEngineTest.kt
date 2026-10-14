package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingOverrideEngine()
        val item = newIdlingOverride("code-224", "title-643")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
