package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledParcelDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledParcelDigestEngine()
        val item = newScheduledParcelDigest("code-931", "label-861")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
