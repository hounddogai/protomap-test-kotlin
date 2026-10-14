package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingZoneMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingZoneMappingEngine(newRegistry())
        val item = newRollingZoneMapping("code-505", "label-162")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
