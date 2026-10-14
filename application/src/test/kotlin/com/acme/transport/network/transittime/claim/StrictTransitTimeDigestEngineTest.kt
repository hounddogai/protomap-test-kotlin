package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTransitTimeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTransitTimeDigestEngine()
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
