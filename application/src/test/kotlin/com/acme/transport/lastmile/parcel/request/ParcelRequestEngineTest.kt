package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelRequestEngine()
        val item = newParcelRequest("code-260", "label-253")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
