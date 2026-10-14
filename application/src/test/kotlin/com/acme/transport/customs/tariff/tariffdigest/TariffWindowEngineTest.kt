package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffWindowEngine(newRegistry())
        val item = newTariffWindow("code-106", "label-936")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
