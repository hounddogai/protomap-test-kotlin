package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedParcelDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedParcelDigestEngine(newRegistry())
        val item = newCappedParcelDigest("code-100", "title-881")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
