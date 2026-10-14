package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackQualificationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackQualificationMilestoneEngine(newRegistry())
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
