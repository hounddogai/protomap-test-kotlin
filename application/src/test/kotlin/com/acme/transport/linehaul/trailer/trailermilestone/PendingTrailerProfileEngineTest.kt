package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTrailerProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTrailerProfileEngine(newRegistry())
        val item = newPendingTrailerProfile("code-216", "name-917")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
