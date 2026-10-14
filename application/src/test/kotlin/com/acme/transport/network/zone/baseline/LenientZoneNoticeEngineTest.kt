package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientZoneNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientZoneNoticeEngine(newRegistry())
        val item = newLenientZoneNotice("code-648", "name-563")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
