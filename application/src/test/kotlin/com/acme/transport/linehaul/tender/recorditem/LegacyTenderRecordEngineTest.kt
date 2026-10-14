package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTenderRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTenderRecordEngine(newRegistry())
        val item = newLegacyTenderRecord("code-528", "title-773")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
