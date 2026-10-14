package com.acme.transport.network.zone.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyZoneRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyZoneRecordEngine(newRegistry())
        val item = newLegacyZoneRecord("code-925", "title-330")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
