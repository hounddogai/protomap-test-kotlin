package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelQuotaEngine(newRegistry())
        val item = newParcelQuota("code-423", "title-287")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
