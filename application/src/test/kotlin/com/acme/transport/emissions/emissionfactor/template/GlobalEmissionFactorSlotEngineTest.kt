package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalEmissionFactorSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalEmissionFactorSlotEngine(newRegistry())
        val item = newGlobalEmissionFactorSlot("code-41", "name-713")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
