package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredUtilizationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredUtilizationReservationEngine(newRegistry())
        val item = newTieredUtilizationReservation("code-901", "title-993")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
