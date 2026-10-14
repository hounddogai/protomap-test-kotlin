package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledZoneRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledZoneRollupEngine()
        val item = newScheduledZoneRollup("code-616", "name-177")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
