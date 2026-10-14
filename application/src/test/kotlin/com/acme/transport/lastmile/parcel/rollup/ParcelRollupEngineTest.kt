package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelRollupEngine(newRegistry())
        val item = newParcelRollup("code-856", "label-690")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
