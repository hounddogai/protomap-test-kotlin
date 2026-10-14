package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneOverrideEngine()
        val item = newZoneOverride("code-795", "label-340")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
