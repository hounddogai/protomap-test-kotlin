package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneGroupEngine(newRegistry())
        val item = newZoneGroup("code-128", "title-628")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
