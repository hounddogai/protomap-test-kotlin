package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientZoneGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientZoneGroupEngine(newRegistry())
        val item = newLenientZoneGroup("code-740", "name-882")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
