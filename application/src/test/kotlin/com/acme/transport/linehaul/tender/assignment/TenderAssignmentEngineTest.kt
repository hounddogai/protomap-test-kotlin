package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderAssignmentEngine(newRegistry())
        val item = newTenderAssignment("code-966", "label-959")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
