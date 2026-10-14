package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRelayNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRelayNoticeEngine()
        val item = newRegionalRelayNotice("code-364", "name-937")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
