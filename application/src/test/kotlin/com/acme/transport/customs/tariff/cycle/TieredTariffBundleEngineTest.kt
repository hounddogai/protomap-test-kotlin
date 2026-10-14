package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTariffBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTariffBundleEngine()
        val item = newTieredTariffBundle("code-425", "name-827")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
