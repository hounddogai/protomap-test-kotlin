package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelEntryEngine(newRegistry())
        val item = newParcelEntry("code-694", "label-919")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
