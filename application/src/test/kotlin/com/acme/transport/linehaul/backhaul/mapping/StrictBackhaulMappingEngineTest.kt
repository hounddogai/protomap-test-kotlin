package com.acme.transport.linehaul.backhaul.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictBackhaulMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictBackhaulMappingEngine()
        val item = newStrictBackhaulMapping("code-648", "title-149")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
