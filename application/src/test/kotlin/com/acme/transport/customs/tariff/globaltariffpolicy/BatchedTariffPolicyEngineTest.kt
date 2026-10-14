package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTariffPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTariffPolicyEngine(newRegistry())
        val item = newBatchedTariffPolicy("code-171", "name-893")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
