package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultParcelVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultParcelVarianceEngine(newRegistry())
        val item = newDefaultParcelVariance("code-22", "name-534")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
