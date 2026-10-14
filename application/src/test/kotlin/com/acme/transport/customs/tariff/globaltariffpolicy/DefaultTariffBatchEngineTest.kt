package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTariffBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTariffBatchEngine(newRegistry())
        val item = newDefaultTariffBatch("code-833", "title-744")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
