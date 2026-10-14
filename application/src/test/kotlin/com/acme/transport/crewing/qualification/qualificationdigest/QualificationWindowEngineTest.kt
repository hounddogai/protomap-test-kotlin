package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationWindowEngine()
        val item = newQualificationWindow("code-166", "name-682")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
