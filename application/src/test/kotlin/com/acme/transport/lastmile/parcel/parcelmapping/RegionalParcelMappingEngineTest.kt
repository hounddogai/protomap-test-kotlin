package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalParcelMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalParcelMappingEngine(newRegistry())
        val item = newRegionalParcelMapping("code-180", "name-69")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
