package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultZoneGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultZoneGroupEngine(newRegistry())
        val item = newDefaultZoneGroup("code-54", "title-715")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
