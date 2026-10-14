package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTariffBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTariffBatchEngine(newRegistry())
        val item = newRollingTariffBatch("code-607", "title-608")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
