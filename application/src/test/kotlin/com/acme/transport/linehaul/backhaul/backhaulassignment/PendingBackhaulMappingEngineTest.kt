package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBackhaulMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBackhaulMappingEngine()
        val item = newPendingBackhaulMapping("code-491", "name-568")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
