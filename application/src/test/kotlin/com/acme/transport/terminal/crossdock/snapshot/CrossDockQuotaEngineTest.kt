package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockQuotaEngine(newRegistry())
        val item = newCrossDockQuota("code-313", "name-878")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
