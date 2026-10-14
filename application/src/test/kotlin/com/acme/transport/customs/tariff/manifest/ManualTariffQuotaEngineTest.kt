package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTariffQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTariffQuotaEngine(newRegistry())
        val item = newManualTariffQuota("code-974", "title-474")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
