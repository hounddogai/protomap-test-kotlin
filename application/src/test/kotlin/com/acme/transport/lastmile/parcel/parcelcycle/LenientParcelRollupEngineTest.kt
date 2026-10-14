package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientParcelRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientParcelRollupEngine(newRegistry())
        val item = newLenientParcelRollup("code-358", "name-596")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
