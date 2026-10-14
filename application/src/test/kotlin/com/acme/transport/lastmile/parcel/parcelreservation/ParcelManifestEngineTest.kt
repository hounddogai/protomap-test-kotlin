package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelManifestEngine(newRegistry())
        val item = newParcelManifest("code-95", "label-844")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
