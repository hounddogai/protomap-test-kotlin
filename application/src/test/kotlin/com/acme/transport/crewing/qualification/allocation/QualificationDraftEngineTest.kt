package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationDraftEngine(newRegistry())
        val item = newQualificationDraft("code-891", "label-493")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
