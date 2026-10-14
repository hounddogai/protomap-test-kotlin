package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultParcelManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultParcelManifestEngine(newRegistry())
        val item = newDefaultParcelManifest("code-917", "label-625")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
