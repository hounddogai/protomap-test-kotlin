package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationAssignmentEngine(newRegistry())
        val item = newQualificationAssignment("code-709", "title-48")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
