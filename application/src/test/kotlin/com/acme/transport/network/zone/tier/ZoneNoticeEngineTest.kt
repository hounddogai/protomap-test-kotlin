package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneNoticeEngine()
        val item = newZoneNotice("code-373", "title-920")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
