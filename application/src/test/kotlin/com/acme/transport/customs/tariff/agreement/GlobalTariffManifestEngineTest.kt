package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTariffManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTariffManifestEngine(newRegistry())
        val item = newGlobalTariffManifest("code-110", "name-491")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
