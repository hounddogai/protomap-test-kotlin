package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTariffCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTariffCycleEngine(newRegistry())
        val item = newStrictTariffCycle("code-670", "title-209")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
