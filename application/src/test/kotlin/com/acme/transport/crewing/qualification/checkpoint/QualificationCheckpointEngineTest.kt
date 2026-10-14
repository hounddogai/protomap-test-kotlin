package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationCheckpointEngine()
        val item = newQualificationCheckpoint("code-48", "title-228")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
