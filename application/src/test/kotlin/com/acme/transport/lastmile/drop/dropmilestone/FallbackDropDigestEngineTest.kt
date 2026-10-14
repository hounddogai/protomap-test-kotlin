package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDropDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDropDigestEngine()
        val item = newFallbackDropDigest("code-596", "label-489")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
