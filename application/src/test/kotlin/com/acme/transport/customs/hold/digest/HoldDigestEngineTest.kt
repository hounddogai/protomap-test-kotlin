package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldDigestEngine()
        val item = newHoldDigest("code-146", "label-679")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
