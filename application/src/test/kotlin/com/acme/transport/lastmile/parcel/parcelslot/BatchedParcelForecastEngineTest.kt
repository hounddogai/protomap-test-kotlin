package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedParcelForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedParcelForecastEngine(newRegistry())
        val item = newBatchedParcelForecast("code-107", "title-999")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
