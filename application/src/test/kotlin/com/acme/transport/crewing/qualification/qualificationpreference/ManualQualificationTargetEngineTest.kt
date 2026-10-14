package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualQualificationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualQualificationTargetEngine()
        val item = newManualQualificationTarget("code-467", "label-439")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
