package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalParcelVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalParcelVarianceEngine(newRegistry())
        val item = newRegionalParcelVariance("code-504", "name-863")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
