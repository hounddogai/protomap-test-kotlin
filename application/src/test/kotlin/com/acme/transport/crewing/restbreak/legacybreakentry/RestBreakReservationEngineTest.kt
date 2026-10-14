package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakReservationEngine(newRegistry())
        val item = newRestBreakReservation("code-851", "title-893")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
