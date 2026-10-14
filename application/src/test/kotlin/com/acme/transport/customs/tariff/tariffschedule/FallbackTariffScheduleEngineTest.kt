package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackTariffScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackTariffScheduleEngine()
        val item = newFallbackTariffSchedule("code-840", "name-157")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
