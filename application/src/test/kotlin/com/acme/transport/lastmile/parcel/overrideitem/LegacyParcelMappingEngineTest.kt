package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyParcelMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyParcelMappingEngine()
        val item = newLegacyParcelMapping("code-386", "label-530")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
