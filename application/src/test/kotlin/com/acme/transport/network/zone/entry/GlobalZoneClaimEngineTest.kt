package com.acme.transport.network.zone.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalZoneClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalZoneClaimEngine(newRegistry())
        val item = newGlobalZoneClaim("code-259", "title-692")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
