package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCameraEventDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCameraEventDigestEngine()
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
