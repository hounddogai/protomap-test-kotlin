package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionRecordEngine(newRegistry())
        val item = newInspectionRecord("code-934", "title-908")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
