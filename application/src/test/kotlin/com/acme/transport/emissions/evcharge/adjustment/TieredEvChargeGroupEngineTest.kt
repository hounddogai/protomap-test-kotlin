package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEvChargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEvChargeGroupEngine(newRegistry())
        val item = newTieredEvChargeGroup("code-671", "title-473")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
