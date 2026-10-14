package com.acme.transport.crewing.qualification.qualificationoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationProfileEngine()
        val item = newQualificationProfile("code-720", "name-810")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
