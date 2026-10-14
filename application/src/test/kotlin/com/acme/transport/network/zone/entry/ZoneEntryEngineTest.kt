package com.acme.transport.network.zone.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneEntryEngine(newRegistry())
        val item = newZoneEntry("code-534", "title-154")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
