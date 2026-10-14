package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTireRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTireRecordEngine(newRegistry())
        val item = newSeasonalTireRecord("code-988", "label-4")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
