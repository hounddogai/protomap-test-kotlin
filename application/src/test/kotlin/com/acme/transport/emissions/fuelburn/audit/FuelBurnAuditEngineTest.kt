package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnAuditEngine(newRegistry())
        val item = newFuelBurnAudit("code-809", "label-939")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
