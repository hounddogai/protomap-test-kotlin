package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictZoneRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictZoneRecordEngine(newRegistry())
        val item = newStrictZoneRecord("code-389", "title-353")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
