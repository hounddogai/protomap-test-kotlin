package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCapacityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCapacityReservationEngine(newRegistry())
        val item = newTieredCapacityReservation("code-821", "label-500")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
