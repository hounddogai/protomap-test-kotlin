package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationEntryEngine()
        val item = newQualificationEntry("code-988", "label-209")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
