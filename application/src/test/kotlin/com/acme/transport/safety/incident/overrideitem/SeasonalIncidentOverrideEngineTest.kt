package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalIncidentOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalIncidentOverrideEngine(newRegistry())
        val item = newSeasonalIncidentOverride("code-795", "title-693")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
