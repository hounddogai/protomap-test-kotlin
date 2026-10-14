package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredParcelPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredParcelPreferenceEngine()
        val item = newTieredParcelPreference("code-829", "title-514")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
