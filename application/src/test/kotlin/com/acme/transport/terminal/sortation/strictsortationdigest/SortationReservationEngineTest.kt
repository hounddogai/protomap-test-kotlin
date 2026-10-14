package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationReservationEngine(newRegistry())
        val item = newSortationReservation("code-718", "title-198")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
