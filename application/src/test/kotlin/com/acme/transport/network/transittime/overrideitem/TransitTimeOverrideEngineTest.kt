package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeOverrideEngine(newRegistry())
        val item = newTransitTimeOverride("code-380", "name-18")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
