package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffOverrideEngine(newRegistry())
        val item = newTariffOverride("code-151", "title-434")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
