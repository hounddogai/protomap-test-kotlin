package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffRequestEngine(newRegistry())
        val item = newTariffRequest("code-325", "title-144")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
