package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class DropDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropDigestEngine()
        val item = newDropDigest("code-541", "label-620")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
