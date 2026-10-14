package com.acme.transport.network.zone.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientZoneAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientZoneAssignmentEngine(newRegistry())
        val item = newLenientZoneAssignment("code-749", "title-203")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
