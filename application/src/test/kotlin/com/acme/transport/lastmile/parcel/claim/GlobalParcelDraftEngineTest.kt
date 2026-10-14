package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalParcelDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalParcelDraftEngine()
        val item = newGlobalParcelDraft("code-406", "name-504")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
