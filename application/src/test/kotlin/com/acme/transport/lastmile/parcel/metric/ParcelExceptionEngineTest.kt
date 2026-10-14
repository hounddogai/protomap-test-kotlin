package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelExceptionEngine(newRegistry())
        val item = newParcelException("code-621", "label-842")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
