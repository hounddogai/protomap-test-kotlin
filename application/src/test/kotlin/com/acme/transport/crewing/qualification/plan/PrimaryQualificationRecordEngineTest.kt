package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryQualificationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryQualificationRecordEngine()
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
