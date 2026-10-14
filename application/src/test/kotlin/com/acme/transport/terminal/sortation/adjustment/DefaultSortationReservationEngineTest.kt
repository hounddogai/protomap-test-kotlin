package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultSortationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultSortationReservationEngine(newRegistry())
        val item = newDefaultSortationReservation("code-451", "title-854")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
