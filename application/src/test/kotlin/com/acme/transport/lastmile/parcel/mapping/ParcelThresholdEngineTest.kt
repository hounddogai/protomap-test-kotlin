package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelThresholdEngine()
        val item = newParcelThreshold("code-81", "label-988")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
