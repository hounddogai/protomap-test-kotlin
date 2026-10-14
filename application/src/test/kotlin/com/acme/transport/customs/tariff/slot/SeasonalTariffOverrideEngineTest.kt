package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTariffOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTariffOverrideEngine()
        val item = newSeasonalTariffOverride("code-903", "name-823")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
