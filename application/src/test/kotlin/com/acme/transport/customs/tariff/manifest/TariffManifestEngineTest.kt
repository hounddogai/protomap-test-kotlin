package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffManifestEngine()
        val item = newTariffManifest("code-317", "name-817")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
