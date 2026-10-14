package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIdlingMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIdlingMappingEngine(newRegistry())
        val item = newRegionalIdlingMapping("code-341", "title-107")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
