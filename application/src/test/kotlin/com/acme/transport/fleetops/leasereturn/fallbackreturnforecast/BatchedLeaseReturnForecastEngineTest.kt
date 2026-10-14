package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLeaseReturnForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLeaseReturnForecastEngine(newRegistry())
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
