package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTariffDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTariffDraftEngine(newRegistry())
        val item = newRollingTariffDraft("code-332", "name-449")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
