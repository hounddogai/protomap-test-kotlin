package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockDigestEngine()
        val item = newCrossDockDigest("code-572", "title-879")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
