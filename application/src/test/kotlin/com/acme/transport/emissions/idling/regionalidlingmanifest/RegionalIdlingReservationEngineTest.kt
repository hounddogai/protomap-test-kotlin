package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIdlingReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIdlingReservationEngine(newRegistry())
        val item = newRegionalIdlingReservation("code-664", "label-998")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
