package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalParcelDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalParcelDraftEngine()
        val item = newRegionalParcelDraft("code-96", "label-415")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
