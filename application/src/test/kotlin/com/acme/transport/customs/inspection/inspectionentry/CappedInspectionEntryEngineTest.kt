package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedInspectionEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedInspectionEntryEngine(newRegistry())
        val item = newCappedInspectionEntry("code-106", "title-259")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
