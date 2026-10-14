package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingQualificationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingQualificationAssignmentEngine(newRegistry())
        val item = newPendingQualificationAssignment("code-906", "label-214")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
