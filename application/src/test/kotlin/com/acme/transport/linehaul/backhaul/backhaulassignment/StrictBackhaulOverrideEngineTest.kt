package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictBackhaulOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictBackhaulOverrideEngine()
        val item = newStrictBackhaulOverride("code-249", "title-902")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
