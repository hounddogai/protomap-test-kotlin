package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceMappingEngine(newRegistry())
        val item = newClearanceMapping("code-913", "title-322")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
