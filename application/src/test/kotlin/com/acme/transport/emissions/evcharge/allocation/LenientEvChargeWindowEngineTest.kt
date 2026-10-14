package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientEvChargeWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientEvChargeWindowEngine(newRegistry())
        val item = newLenientEvChargeWindow("code-848", "name-102")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
