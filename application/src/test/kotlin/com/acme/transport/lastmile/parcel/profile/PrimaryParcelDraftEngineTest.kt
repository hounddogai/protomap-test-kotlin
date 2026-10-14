package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryParcelDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryParcelDraftEngine()
        val item = newPrimaryParcelDraft("code-594", "label-332")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
