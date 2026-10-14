package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationSlotEngine()
        val item = newQualificationSlot("code-947", "title-118")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
