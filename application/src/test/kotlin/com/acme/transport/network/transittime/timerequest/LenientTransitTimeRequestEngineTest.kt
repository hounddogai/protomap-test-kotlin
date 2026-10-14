package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTransitTimeRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTransitTimeRequestEngine(newRegistry())
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
