package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulMappingEngine(newRegistry())
        val item = newBackhaulMapping("code-32", "label-732")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
