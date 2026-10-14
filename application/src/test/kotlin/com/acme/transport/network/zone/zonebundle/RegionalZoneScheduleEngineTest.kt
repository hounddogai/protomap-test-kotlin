package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalZoneScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalZoneScheduleEngine(newRegistry())
        val item = newRegionalZoneSchedule("code-186", "title-488")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
