package com.acme.transport.customs.tariff.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffScheduleEngine()
        val item = newTariffSchedule("code-919", "title-155")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
