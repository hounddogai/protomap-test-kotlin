package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredParcelBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredParcelBaselineEngine()
        val item = newTieredParcelBaseline("code-104", "title-154")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
