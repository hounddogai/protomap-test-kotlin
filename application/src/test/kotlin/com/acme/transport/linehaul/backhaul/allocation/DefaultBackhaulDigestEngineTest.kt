package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBackhaulDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultBackhaulDigestEngine()
        val item = newDefaultBackhaulDigest("code-348", "name-407")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
