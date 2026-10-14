package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalZoneQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalZoneQuotaEngine(newRegistry())
        val item = newRegionalZoneQuota("code-203", "name-479")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
